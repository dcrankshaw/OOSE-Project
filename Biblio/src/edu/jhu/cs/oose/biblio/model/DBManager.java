package edu.jhu.cs.oose.biblio.model;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class DBManager {
	
	private EntityManagerFactory entityManagerFactory;
	
	public DBManager() {
		entityManagerFactory = Persistence.createEntityManagerFactory( "edu.jhu.cs.oose.biblio.model.jpa" );
	}
	
	public void store(Object obj) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();
		entityManager.persist(obj);
		entityManager.getTransaction().commit();
		entityManager.close();
	}
	
	// Because of generics, this isn't type safe.
	// we should change something so that we don't have to
	// cast everything everywhere. - Paul
	public Collection<?> get(String query) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();
		return entityManager.createQuery( query ).getResultList();
	}
}
