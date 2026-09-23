package com.bookshelf.testsupport;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Gives every integration-test ApplicationContext its own Postgres schema, so two
 * contexts' create-drop cycles can never collide. Registered through
 * META-INF/spring.factories; no-ops unless the integrationtest profile is active.
 */
public class UniqueSchemaPerContextInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Set<String> CREATED = ConcurrentHashMap.newKeySet();
    private static volatile boolean dropHookRegistered = false;

    @Override
    public void initialize( ConfigurableApplicationContext context ) {
        ConfigurableEnvironment env = context.getEnvironment();

        if ( !Arrays.asList( env.getActiveProfiles() ).contains( "integrationtest" ) ) {
            return;
        }

        String url = env.getProperty( "spring.datasource.url" );
        String user = env.getProperty( "spring.datasource.username" );
        String password = env.getProperty( "spring.datasource.password" );
        String schema = "it_" + UUID.randomUUID().toString().replace( "-", "" ).substring( 0, 12 );

        execute( url, user, password, "create schema if not exists " + schema );
        CREATED.add( schema );
        registerDropHook( url, user, password );

        TestPropertyValues.of( "spring.datasource.url=" + url
                + ( url.contains( "?" ) ? "&" : "?" ) + "currentSchema=" + schema ).applyTo( env );
    }

    private static synchronized void registerDropHook( String url, String user, String password ) {
        if ( dropHookRegistered ) {
            return;
        }
        dropHookRegistered = true;
        Runtime.getRuntime().addShutdownHook( new Thread( () -> {
            for ( String schema : CREATED ) {
                try {
                    execute( url, user, password, "drop schema if exists " + schema + " cascade" );
                }
                catch ( RuntimeException ignored ) {
                    // best effort
                }
            }
        } ) );
    }

    private static void execute( String url, String user, String password, String sql ) {
        try ( Connection c = DriverManager.getConnection( url, user, password );
                Statement s = c.createStatement() ) {
            s.execute( sql );
        }
        catch ( Exception e ) {
            throw new IllegalStateException( "Could not run '" + sql + "' on " + url
                    + " - is Postgres up? ( make docker-up )", e );
        }
    }
}
