/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.controllers;

import com.controllers.exceptions.NonexistentEntityException;
import com.entities.Servicio;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import com.entities.Vehiculo;
import com.utils.JPAUtil;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author juandiego
 */
public class ServicioJpaController implements Serializable {

    public ServicioJpaController() {
        this.emf = JPAUtil.getEntityManagerFactory();
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Servicio servicio) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Vehiculo vehiculoPlaca = servicio.getVehiculoPlaca();
            if (vehiculoPlaca != null) {
                vehiculoPlaca = em.getReference(vehiculoPlaca.getClass(), vehiculoPlaca.getPlaca());
                servicio.setVehiculoPlaca(vehiculoPlaca);
            }
            em.persist(servicio);
            if (vehiculoPlaca != null) {
                vehiculoPlaca.getServicioList().add(servicio);
                vehiculoPlaca = em.merge(vehiculoPlaca);
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Servicio servicio) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Servicio persistentServicio = em.find(Servicio.class, servicio.getId());
            Vehiculo vehiculoPlacaOld = persistentServicio.getVehiculoPlaca();
            Vehiculo vehiculoPlacaNew = servicio.getVehiculoPlaca();
            if (vehiculoPlacaNew != null) {
                vehiculoPlacaNew = em.getReference(vehiculoPlacaNew.getClass(), vehiculoPlacaNew.getPlaca());
                servicio.setVehiculoPlaca(vehiculoPlacaNew);
            }
            servicio = em.merge(servicio);
            if (vehiculoPlacaOld != null && !vehiculoPlacaOld.equals(vehiculoPlacaNew)) {
                vehiculoPlacaOld.getServicioList().remove(servicio);
                vehiculoPlacaOld = em.merge(vehiculoPlacaOld);
            }
            if (vehiculoPlacaNew != null && !vehiculoPlacaNew.equals(vehiculoPlacaOld)) {
                vehiculoPlacaNew.getServicioList().add(servicio);
                vehiculoPlacaNew = em.merge(vehiculoPlacaNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = servicio.getId();
                if (findServicio(id) == null) {
                    throw new NonexistentEntityException("The servicio with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Servicio servicio;
            try {
                servicio = em.getReference(Servicio.class, id);
                servicio.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The servicio with id " + id + " no longer exists.", enfe);
            }
            Vehiculo vehiculoPlaca = servicio.getVehiculoPlaca();
            if (vehiculoPlaca != null) {
                vehiculoPlaca.getServicioList().remove(servicio);
                vehiculoPlaca = em.merge(vehiculoPlaca);
            }
            em.remove(servicio);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Servicio> findServicioEntities() {
        return findServicioEntities(true, -1, -1);
    }

    public List<Servicio> findServicioEntities(int maxResults, int firstResult) {
        return findServicioEntities(false, maxResults, firstResult);
    }

    private List<Servicio> findServicioEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Servicio.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Servicio findServicio(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Servicio.class, id);
        } finally {
            em.close();
        }
    }

    public int getServicioCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Servicio> rt = cq.from(Servicio.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
