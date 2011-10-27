package edu.jhu.cs.oose.biblio.model;

import java.util.Collection;
import java.util.List;

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
	
	public Collection<?> get(String query) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();
		return entityManager.createQuery( query ).getResultList();
	}
}
