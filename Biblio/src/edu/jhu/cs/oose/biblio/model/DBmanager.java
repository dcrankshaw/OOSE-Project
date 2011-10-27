package edu.jhu.cs.oose.biblio.model;

import java.util.Collection;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class DBmanager {
	
	private EntityManagerFactory entityManagerFactory;
	
	public DBmanager() {
		entityManagerFactory = Persistence.createEntityManagerFactory( "iTag.jpa" );
	}
	
	public void store(Object obj) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();
		entityManager.persist(obj);
		entityManager.getTransaction().commit();
		entityManager.close();
	}
	
	public Collection get(String query) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();
		return entityManager.createQuery( query ).getResultList();
	}
}
