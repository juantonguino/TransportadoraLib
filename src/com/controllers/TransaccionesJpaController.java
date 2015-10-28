/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.controllers;

import com.controllers.exceptions.NonexistentEntityException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import com.entities.Sucursal;
import com.entities.Transacciones;
import com.entities.Vehiculo;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author juandiego
 */
public class TransaccionesJpaController implements Serializable {

    public TransaccionesJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Transacciones transacciones) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Sucursal sucursalId = transacciones.getSucursalId();
            if (sucursalId != null) {
                sucursalId = em.getReference(sucursalId.getClass(), sucursalId.getId());
                transacciones.setSucursalId(sucursalId);
            }
            Vehiculo vehiculoPlaca = transacciones.getVehiculoPlaca();
            if (vehiculoPlaca != null) {
                vehiculoPlaca = em.getReference(vehiculoPlaca.getClass(), vehiculoPlaca.getPlaca());
                transacciones.setVehiculoPlaca(vehiculoPlaca);
            }
            em.persist(transacciones);
            if (sucursalId != null) {
                sucursalId.getTransaccionesList().add(transacciones);
                sucursalId = em.merge(sucursalId);
            }
            if (vehiculoPlaca != null) {
                vehiculoPlaca.getTransaccionesList().add(transacciones);
                vehiculoPlaca = em.merge(vehiculoPlaca);
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Transacciones transacciones) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Transacciones persistentTransacciones = em.find(Transacciones.class, transacciones.getId());
            Sucursal sucursalIdOld = persistentTransacciones.getSucursalId();
            Sucursal sucursalIdNew = transacciones.getSucursalId();
            Vehiculo vehiculoPlacaOld = persistentTransacciones.getVehiculoPlaca();
            Vehiculo vehiculoPlacaNew = transacciones.getVehiculoPlaca();
            if (sucursalIdNew != null) {
                sucursalIdNew = em.getReference(sucursalIdNew.getClass(), sucursalIdNew.getId());
                transacciones.setSucursalId(sucursalIdNew);
            }
            if (vehiculoPlacaNew != null) {
                vehiculoPlacaNew = em.getReference(vehiculoPlacaNew.getClass(), vehiculoPlacaNew.getPlaca());
                transacciones.setVehiculoPlaca(vehiculoPlacaNew);
            }
            transacciones = em.merge(transacciones);
            if (sucursalIdOld != null && !sucursalIdOld.equals(sucursalIdNew)) {
                sucursalIdOld.getTransaccionesList().remove(transacciones);
                sucursalIdOld = em.merge(sucursalIdOld);
            }
            if (sucursalIdNew != null && !sucursalIdNew.equals(sucursalIdOld)) {
                sucursalIdNew.getTransaccionesList().add(transacciones);
                sucursalIdNew = em.merge(sucursalIdNew);
            }
            if (vehiculoPlacaOld != null && !vehiculoPlacaOld.equals(vehiculoPlacaNew)) {
                vehiculoPlacaOld.getTransaccionesList().remove(transacciones);
                vehiculoPlacaOld = em.merge(vehiculoPlacaOld);
            }
            if (vehiculoPlacaNew != null && !vehiculoPlacaNew.equals(vehiculoPlacaOld)) {
                vehiculoPlacaNew.getTransaccionesList().add(transacciones);
                vehiculoPlacaNew = em.merge(vehiculoPlacaNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = transacciones.getId();
                if (findTransacciones(id) == null) {
                    throw new NonexistentEntityException("The transacciones with id " + id + " no longer exists.");
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
            Transacciones transacciones;
            try {
                transacciones = em.getReference(Transacciones.class, id);
                transacciones.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The transacciones with id " + id + " no longer exists.", enfe);
            }
            Sucursal sucursalId = transacciones.getSucursalId();
            if (sucursalId != null) {
                sucursalId.getTransaccionesList().remove(transacciones);
                sucursalId = em.merge(sucursalId);
            }
            Vehiculo vehiculoPlaca = transacciones.getVehiculoPlaca();
            if (vehiculoPlaca != null) {
                vehiculoPlaca.getTransaccionesList().remove(transacciones);
                vehiculoPlaca = em.merge(vehiculoPlaca);
            }
            em.remove(transacciones);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Transacciones> findTransaccionesEntities() {
        return findTransaccionesEntities(true, -1, -1);
    }

    public List<Transacciones> findTransaccionesEntities(int maxResults, int firstResult) {
        return findTransaccionesEntities(false, maxResults, firstResult);
    }

    private List<Transacciones> findTransaccionesEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Transacciones.class));
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

    public Transacciones findTransacciones(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Transacciones.class, id);
        } finally {
            em.close();
        }
    }

    public int getTransaccionesCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Transacciones> rt = cq.from(Transacciones.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
