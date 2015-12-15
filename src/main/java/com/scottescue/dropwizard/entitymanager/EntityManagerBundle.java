package com.scottescue.dropwizard.entitymanager;

import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import com.google.common.collect.ImmutableList;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.db.DatabaseConfiguration;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import javax.persistence.EntityManagerFactory;


public abstract class EntityManagerBundle<T extends Configuration> implements ConfiguredBundle<T>, DatabaseConfiguration<T> {
    public static final String DEFAULT_NAME = "hibernate-jpa";

    private EntityManagerFactory entityManagerFactory;

    private final ImmutableList<Class<?>> entities;
    private final EntityManagerFactoryFactory entityManagerFactoryFactory;

    protected EntityManagerBundle(Class<?> entity, Class<?>... entities) {
        this(ImmutableList.<Class<?>>builder().add(entity).add(entities).build(),
                new EntityManagerFactoryFactory());
    }

    protected EntityManagerBundle(ImmutableList<Class<?>> entities,
                                  EntityManagerFactoryFactory entityManagerFactoryFactory) {
        this.entities = entities;
        this.entityManagerFactoryFactory = entityManagerFactoryFactory;
    }

    public void run(T configuration, Environment environment) throws Exception {
        final PooledDataSourceFactory dbConfig = getDataSourceFactory(configuration);
        this.entityManagerFactory = entityManagerFactoryFactory.build(this, environment, dbConfig, entities, name());
    }

    public void initialize(Bootstrap<?> bootstrap) {
        bootstrap.getObjectMapper().registerModule(createHibernate4Module());
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    /**
     * Override to configure the {@link Hibernate4Module}.
     */
    protected Hibernate4Module createHibernate4Module() {
        return new Hibernate4Module();
    }

    /**
     * Override to configure the name of the bundle
     * (It's used for the bundle health check and database pool metrics)
     */
    protected String name() {
        return DEFAULT_NAME;
    }

    /**
     * Override to configure JPA persistence unit.
     * @param configuration the persistence unit configuration
     */
    protected void configure(PersistenceUnitConfig configuration) {
    }
}