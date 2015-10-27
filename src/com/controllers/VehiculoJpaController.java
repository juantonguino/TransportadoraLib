/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.controllers;

import com.controllers.exceptions.IllegalOrphanException;
import com.controllers.exceptions.NonexistentEntityException;
import com.controllers.exceptions.PreexistingEntityException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import com.entities.Sucursal;
import com.entities.Servicio;
import java.util.ArrayList;
import java.util.List;
import com.entities.Transacciones;
import com.entities.Vehiculo;
import com.utils.JPAUtil;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author juandiego
 */
public class VehiculoJpaController implements Serializable {

    public VehiculoJpaController() {
        this.emf = JPAUtil.getEntityManagerFactory();
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Vehiculo vehiculo) throws PreexistingEntityException, Exception {
        if (vehiculo.getServicioList() == null) {
            vehiculo.setServicioList(new ArrayList<Servicio>());
        }
        if (vehiculo.getTransaccionesList() == null) {
            vehiculo.setTransaccionesList(new ArrayList<Transacciones>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Sucursal sucursalId = vehiculo.getSucursalId();
            if (sucursalId != null) {
                sucursalId = em.getReference(sucursalId.getClass(), sucursalId.getId());
                vehiculo.setSucursalId(sucursalId);
            }
            List<Servicio> attachedServicioList = new ArrayList<Servicio>();
            for (Servicio servicioListServicioToAttach : vehiculo.getServicioList()) {
                servicioListServicioToAttach = em.getReference(servicioListServicioToAttach.getClass(), servicioListServicioToAttach.getId());
                attachedServicioList.add(servicioListServicioToAttach);
            }
            vehiculo.setServicioList(attachedServicioList);
            List<Transacciones> attachedTransaccionesList = new ArrayList<Transacciones>();
            for (Transacciones transaccionesListTransaccionesToAttach : vehiculo.getTransaccionesList()) {
                transaccionesListTransaccionesToAttach = em.getReference(transaccionesListTransaccionesToAttach.getClass(), transaccionesListTransaccionesToAttach.getId());
                attachedTransaccionesList.add(transaccionesListTransaccionesToAttach);
            }
            vehiculo.setTransaccionesList(attachedTransaccionesList);
            em.persist(vehiculo);
            if (sucursalId != null) {
                sucursalId.getVehiculoList().add(vehiculo);
                sucursalId = em.merge(sucursalId);
            }
            for (Servicio servicioListServicio : vehiculo.getServicioList()) {
                Vehiculo oldVehiculoPlacaOfServicioListServicio = servicioListServicio.getVehiculoPlaca();
                servicioListServicio.setVehiculoPlaca(vehiculo);
                servicioListServicio = em.merge(servicioListServicio);
                if (oldVehiculoPlacaOfServicioListServicio != null) {
                    oldVehiculoPlacaOfServicioListServicio.getServicioList().remove(servicioListServicio);
                    oldVehiculoPlacaOfServicioListServicio = em.merge(oldVehiculoPlacaOfServicioListServicio);
                }
            }
            for (Transacciones transaccionesListTransacciones : vehiculo.getTransaccionesList()) {
                Vehiculo oldVehiculoPlacaOfTransaccionesListTransacciones = transaccionesListTransacciones.getVehiculoPlaca();
                transaccionesListTransacciones.setVehiculoPlaca(vehiculo);
                transaccionesListTransacciones = em.merge(transaccionesListTransacciones);
                if (oldVehiculoPlacaOfTransaccionesListTransacciones != null) {
                    oldVehiculoPlacaOfTransaccionesListTransacciones.getTransaccionesList().remove(transaccionesListTransacciones);
                    oldVehiculoPlacaOfTransaccionesListTransacciones = em.merge(oldVehiculoPlacaOfTransaccionesListTransacciones);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findVehiculo(vehiculo.getPlaca()) != null) {
                throw new PreexistingEntityException("Vehiculo " + vehiculo + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Vehiculo vehiculo) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Vehiculo persistentVehiculo = em.find(Vehiculo.class, vehiculo.getPlaca());
            Sucursal sucursalIdOld = persistentVehiculo.getSucursalId();
            Sucursal sucursalIdNew = vehiculo.getSucursalId();
            List<Servicio> servicioListOld = persistentVehiculo.getServicioList();
            List<Servicio> servicioListNew = vehiculo.getServicioList();
            List<Transacciones> transaccionesListOld = persistentVehiculo.getTransaccionesList();
            List<Transacciones> transaccionesListNew = vehiculo.getTransaccionesList();
            List<String> illegalOrphanMessages = null;
            for (Servicio servicioListOldServicio : servicioListOld) {
                if (!servicioListNew.contains(servicioListOldServicio)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Servicio " + servicioListOldServicio + " since its vehiculoPlaca field is not nullable.");
                }
            }
            for (Transacciones transaccionesListOldTransacciones : transaccionesListOld) {
                if (!transaccionesListNew.contains(transaccionesListOldTransacciones)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Transacciones " + transaccionesListOldTransacciones + " since its vehiculoPlaca field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (sucursalIdNew != null) {
                sucursalIdNew = em.getReference(sucursalIdNew.getClass(), sucursalIdNew.getId());
                vehiculo.setSucursalId(sucursalIdNew);
            }
            List<Servicio> attachedServicioListNew = new ArrayList<Servicio>();
            for (Servicio servicioListNewServicioToAttach : servicioListNew) {
                servicioListNewServicioToAttach = em.getReference(servicioListNewServicioToAttach.getClass(), servicioListNewServicioToAttach.getId());
                attachedServicioListNew.add(servicioListNewServicioToAttach);
            }
            servicioListNew = attachedServicioListNew;
            vehiculo.setServicioList(servicioListNew);
            List<Transacciones> attachedTransaccionesListNew = new ArrayList<Transacciones>();
            for (Transacciones transaccionesListNewTransaccionesToAttach : transaccionesListNew) {
                transaccionesListNewTransaccionesToAttach = em.getReference(transaccionesListNewTransaccionesToAttach.getClass(), transaccionesListNewTransaccionesToAttach.getId());
                attachedTransaccionesListNew.add(transaccionesListNewTransaccionesToAttach);
            }
            transaccionesListNew = attachedTransaccionesListNew;
            vehiculo.setTransaccionesList(transaccionesListNew);
            vehiculo = em.merge(vehiculo);
            if (sucursalIdOld != null && !sucursalIdOld.equals(sucursalIdNew)) {
                sucursalIdOld.getVehiculoList().remove(vehiculo);
                sucursalIdOld = em.merge(sucursalIdOld);
            }
            if (sucursalIdNew != null && !sucursalIdNew.equals(sucursalIdOld)) {
                sucursalIdNew.getVehiculoList().add(vehiculo);
                sucursalIdNew = em.merge(sucursalIdNew);
            }
            for (Servicio servicioListNewServicio : servicioListNew) {
                if (!servicioListOld.contains(servicioListNewServicio)) {
                    Vehiculo oldVehiculoPlacaOfServicioListNewServicio = servicioListNewServicio.getVehiculoPlaca();
                    servicioListNewServicio.setVehiculoPlaca(vehiculo);
                    servicioListNewServicio = em.merge(servicioListNewServicio);
                    if (oldVehiculoPlacaOfServicioListNewServicio != null && !oldVehiculoPlacaOfServicioListNewServicio.equals(vehiculo)) {
                        oldVehiculoPlacaOfServicioListNewServicio.getServicioList().remove(servicioListNewServicio);
                        oldVehiculoPlacaOfServicioListNewServicio = em.merge(oldVehiculoPlacaOfServicioListNewServicio);
                    }
                }
            }
            for (Transacciones transaccionesListNewTransacciones : transaccionesListNew) {
                if (!transaccionesListOld.contains(transaccionesListNewTransacciones)) {
                    Vehiculo oldVehiculoPlacaOfTransaccionesListNewTransacciones = transaccionesListNewTransacciones.getVehiculoPlaca();
                    transaccionesListNewTransacciones.setVehiculoPlaca(vehiculo);
                    transaccionesListNewTransacciones = em.merge(transaccionesListNewTransacciones);
                    if (oldVehiculoPlacaOfTransaccionesListNewTransacciones != null && !oldVehiculoPlacaOfTransaccionesListNewTransacciones.equals(vehiculo)) {
                        oldVehiculoPlacaOfTransaccionesListNewTransacciones.getTransaccionesList().remove(transaccionesListNewTransacciones);
                        oldVehiculoPlacaOfTransaccionesListNewTransacciones = em.merge(oldVehiculoPlacaOfTransaccionesListNewTransacciones);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                String id = vehiculo.getPlaca();
                if (findVehiculo(id) == null) {
                    throw new NonexistentEntityException("The vehiculo with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(String id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Vehiculo vehiculo;
            try {
                vehiculo = em.getReference(Vehiculo.class, id);
                vehiculo.getPlaca();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The vehiculo with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Servicio> servicioListOrphanCheck = vehiculo.getServicioList();
            for (Servicio servicioListOrphanCheckServicio : servicioListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Vehiculo (" + vehiculo + ") cannot be destroyed since the Servicio " + servicioListOrphanCheckServicio + " in its servicioList field has a non-nullable vehiculoPlaca field.");
            }
            List<Transacciones> transaccionesListOrphanCheck = vehiculo.getTransaccionesList();
            for (Transacciones transaccionesListOrphanCheckTransacciones : transaccionesListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Vehiculo (" + vehiculo + ") cannot be destroyed since the Transacciones " + transaccionesListOrphanCheckTransacciones + " in its transaccionesList field has a non-nullable vehiculoPlaca field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Sucursal sucursalId = vehiculo.getSucursalId();
            if (sucursalId != null) {
                sucursalId.getVehiculoList().remove(vehiculo);
                sucursalId = em.merge(sucursalId);
            }
            em.remove(vehiculo);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Vehiculo> findVehiculoEntities() {
        return findVehiculoEntities(true, -1, -1);
    }

    public List<Vehiculo> findVehiculoEntities(int maxResults, int firstResult) {
        return findVehiculoEntities(false, maxResults, firstResult);
    }

    private List<Vehiculo> findVehiculoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Vehiculo.class));
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

    public Vehiculo findVehiculo(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Vehiculo.class, id);
        } finally {
            em.close();
        }
    }

    public int getVehiculoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Vehiculo> rt = cq.from(Vehiculo.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
