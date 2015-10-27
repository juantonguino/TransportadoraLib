/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.controllers;

import com.controllers.exceptions.IllegalOrphanException;
import com.controllers.exceptions.NonexistentEntityException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import com.entities.Cliente;
import com.entities.Sucursal;
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
public class SucursalJpaController implements Serializable {

    public SucursalJpaController() {
        this.emf = JPAUtil.getEntityManagerFactory();
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Sucursal sucursal) {
        if (sucursal.getClienteList() == null) {
            sucursal.setClienteList(new ArrayList<Cliente>());
        }
        if (sucursal.getTransaccionesList() == null) {
            sucursal.setTransaccionesList(new ArrayList<Transacciones>());
        }
        if (sucursal.getVehiculoList() == null) {
            sucursal.setVehiculoList(new ArrayList<Vehiculo>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            List<Cliente> attachedClienteList = new ArrayList<Cliente>();
            for (Cliente clienteListClienteToAttach : sucursal.getClienteList()) {
                clienteListClienteToAttach = em.getReference(clienteListClienteToAttach.getClass(), clienteListClienteToAttach.getIdentificacion());
                attachedClienteList.add(clienteListClienteToAttach);
            }
            sucursal.setClienteList(attachedClienteList);
            List<Transacciones> attachedTransaccionesList = new ArrayList<Transacciones>();
            for (Transacciones transaccionesListTransaccionesToAttach : sucursal.getTransaccionesList()) {
                transaccionesListTransaccionesToAttach = em.getReference(transaccionesListTransaccionesToAttach.getClass(), transaccionesListTransaccionesToAttach.getId());
                attachedTransaccionesList.add(transaccionesListTransaccionesToAttach);
            }
            sucursal.setTransaccionesList(attachedTransaccionesList);
            List<Vehiculo> attachedVehiculoList = new ArrayList<Vehiculo>();
            for (Vehiculo vehiculoListVehiculoToAttach : sucursal.getVehiculoList()) {
                vehiculoListVehiculoToAttach = em.getReference(vehiculoListVehiculoToAttach.getClass(), vehiculoListVehiculoToAttach.getPlaca());
                attachedVehiculoList.add(vehiculoListVehiculoToAttach);
            }
            sucursal.setVehiculoList(attachedVehiculoList);
            em.persist(sucursal);
            for (Cliente clienteListCliente : sucursal.getClienteList()) {
                Sucursal oldSucursalIdOfClienteListCliente = clienteListCliente.getSucursalId();
                clienteListCliente.setSucursalId(sucursal);
                clienteListCliente = em.merge(clienteListCliente);
                if (oldSucursalIdOfClienteListCliente != null) {
                    oldSucursalIdOfClienteListCliente.getClienteList().remove(clienteListCliente);
                    oldSucursalIdOfClienteListCliente = em.merge(oldSucursalIdOfClienteListCliente);
                }
            }
            for (Transacciones transaccionesListTransacciones : sucursal.getTransaccionesList()) {
                Sucursal oldSucursalIdOfTransaccionesListTransacciones = transaccionesListTransacciones.getSucursalId();
                transaccionesListTransacciones.setSucursalId(sucursal);
                transaccionesListTransacciones = em.merge(transaccionesListTransacciones);
                if (oldSucursalIdOfTransaccionesListTransacciones != null) {
                    oldSucursalIdOfTransaccionesListTransacciones.getTransaccionesList().remove(transaccionesListTransacciones);
                    oldSucursalIdOfTransaccionesListTransacciones = em.merge(oldSucursalIdOfTransaccionesListTransacciones);
                }
            }
            for (Vehiculo vehiculoListVehiculo : sucursal.getVehiculoList()) {
                Sucursal oldSucursalIdOfVehiculoListVehiculo = vehiculoListVehiculo.getSucursalId();
                vehiculoListVehiculo.setSucursalId(sucursal);
                vehiculoListVehiculo = em.merge(vehiculoListVehiculo);
                if (oldSucursalIdOfVehiculoListVehiculo != null) {
                    oldSucursalIdOfVehiculoListVehiculo.getVehiculoList().remove(vehiculoListVehiculo);
                    oldSucursalIdOfVehiculoListVehiculo = em.merge(oldSucursalIdOfVehiculoListVehiculo);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Sucursal sucursal) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Sucursal persistentSucursal = em.find(Sucursal.class, sucursal.getId());
            List<Cliente> clienteListOld = persistentSucursal.getClienteList();
            List<Cliente> clienteListNew = sucursal.getClienteList();
            List<Transacciones> transaccionesListOld = persistentSucursal.getTransaccionesList();
            List<Transacciones> transaccionesListNew = sucursal.getTransaccionesList();
            List<Vehiculo> vehiculoListOld = persistentSucursal.getVehiculoList();
            List<Vehiculo> vehiculoListNew = sucursal.getVehiculoList();
            List<String> illegalOrphanMessages = null;
            for (Cliente clienteListOldCliente : clienteListOld) {
                if (!clienteListNew.contains(clienteListOldCliente)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Cliente " + clienteListOldCliente + " since its sucursalId field is not nullable.");
                }
            }
            for (Transacciones transaccionesListOldTransacciones : transaccionesListOld) {
                if (!transaccionesListNew.contains(transaccionesListOldTransacciones)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Transacciones " + transaccionesListOldTransacciones + " since its sucursalId field is not nullable.");
                }
            }
            for (Vehiculo vehiculoListOldVehiculo : vehiculoListOld) {
                if (!vehiculoListNew.contains(vehiculoListOldVehiculo)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Vehiculo " + vehiculoListOldVehiculo + " since its sucursalId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            List<Cliente> attachedClienteListNew = new ArrayList<Cliente>();
            for (Cliente clienteListNewClienteToAttach : clienteListNew) {
                clienteListNewClienteToAttach = em.getReference(clienteListNewClienteToAttach.getClass(), clienteListNewClienteToAttach.getIdentificacion());
                attachedClienteListNew.add(clienteListNewClienteToAttach);
            }
            clienteListNew = attachedClienteListNew;
            sucursal.setClienteList(clienteListNew);
            List<Transacciones> attachedTransaccionesListNew = new ArrayList<Transacciones>();
            for (Transacciones transaccionesListNewTransaccionesToAttach : transaccionesListNew) {
                transaccionesListNewTransaccionesToAttach = em.getReference(transaccionesListNewTransaccionesToAttach.getClass(), transaccionesListNewTransaccionesToAttach.getId());
                attachedTransaccionesListNew.add(transaccionesListNewTransaccionesToAttach);
            }
            transaccionesListNew = attachedTransaccionesListNew;
            sucursal.setTransaccionesList(transaccionesListNew);
            List<Vehiculo> attachedVehiculoListNew = new ArrayList<Vehiculo>();
            for (Vehiculo vehiculoListNewVehiculoToAttach : vehiculoListNew) {
                vehiculoListNewVehiculoToAttach = em.getReference(vehiculoListNewVehiculoToAttach.getClass(), vehiculoListNewVehiculoToAttach.getPlaca());
                attachedVehiculoListNew.add(vehiculoListNewVehiculoToAttach);
            }
            vehiculoListNew = attachedVehiculoListNew;
            sucursal.setVehiculoList(vehiculoListNew);
            sucursal = em.merge(sucursal);
            for (Cliente clienteListNewCliente : clienteListNew) {
                if (!clienteListOld.contains(clienteListNewCliente)) {
                    Sucursal oldSucursalIdOfClienteListNewCliente = clienteListNewCliente.getSucursalId();
                    clienteListNewCliente.setSucursalId(sucursal);
                    clienteListNewCliente = em.merge(clienteListNewCliente);
                    if (oldSucursalIdOfClienteListNewCliente != null && !oldSucursalIdOfClienteListNewCliente.equals(sucursal)) {
                        oldSucursalIdOfClienteListNewCliente.getClienteList().remove(clienteListNewCliente);
                        oldSucursalIdOfClienteListNewCliente = em.merge(oldSucursalIdOfClienteListNewCliente);
                    }
                }
            }
            for (Transacciones transaccionesListNewTransacciones : transaccionesListNew) {
                if (!transaccionesListOld.contains(transaccionesListNewTransacciones)) {
                    Sucursal oldSucursalIdOfTransaccionesListNewTransacciones = transaccionesListNewTransacciones.getSucursalId();
                    transaccionesListNewTransacciones.setSucursalId(sucursal);
                    transaccionesListNewTransacciones = em.merge(transaccionesListNewTransacciones);
                    if (oldSucursalIdOfTransaccionesListNewTransacciones != null && !oldSucursalIdOfTransaccionesListNewTransacciones.equals(sucursal)) {
                        oldSucursalIdOfTransaccionesListNewTransacciones.getTransaccionesList().remove(transaccionesListNewTransacciones);
                        oldSucursalIdOfTransaccionesListNewTransacciones = em.merge(oldSucursalIdOfTransaccionesListNewTransacciones);
                    }
                }
            }
            for (Vehiculo vehiculoListNewVehiculo : vehiculoListNew) {
                if (!vehiculoListOld.contains(vehiculoListNewVehiculo)) {
                    Sucursal oldSucursalIdOfVehiculoListNewVehiculo = vehiculoListNewVehiculo.getSucursalId();
                    vehiculoListNewVehiculo.setSucursalId(sucursal);
                    vehiculoListNewVehiculo = em.merge(vehiculoListNewVehiculo);
                    if (oldSucursalIdOfVehiculoListNewVehiculo != null && !oldSucursalIdOfVehiculoListNewVehiculo.equals(sucursal)) {
                        oldSucursalIdOfVehiculoListNewVehiculo.getVehiculoList().remove(vehiculoListNewVehiculo);
                        oldSucursalIdOfVehiculoListNewVehiculo = em.merge(oldSucursalIdOfVehiculoListNewVehiculo);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = sucursal.getId();
                if (findSucursal(id) == null) {
                    throw new NonexistentEntityException("The sucursal with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Sucursal sucursal;
            try {
                sucursal = em.getReference(Sucursal.class, id);
                sucursal.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The sucursal with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Cliente> clienteListOrphanCheck = sucursal.getClienteList();
            for (Cliente clienteListOrphanCheckCliente : clienteListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Sucursal (" + sucursal + ") cannot be destroyed since the Cliente " + clienteListOrphanCheckCliente + " in its clienteList field has a non-nullable sucursalId field.");
            }
            List<Transacciones> transaccionesListOrphanCheck = sucursal.getTransaccionesList();
            for (Transacciones transaccionesListOrphanCheckTransacciones : transaccionesListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Sucursal (" + sucursal + ") cannot be destroyed since the Transacciones " + transaccionesListOrphanCheckTransacciones + " in its transaccionesList field has a non-nullable sucursalId field.");
            }
            List<Vehiculo> vehiculoListOrphanCheck = sucursal.getVehiculoList();
            for (Vehiculo vehiculoListOrphanCheckVehiculo : vehiculoListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Sucursal (" + sucursal + ") cannot be destroyed since the Vehiculo " + vehiculoListOrphanCheckVehiculo + " in its vehiculoList field has a non-nullable sucursalId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(sucursal);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Sucursal> findSucursalEntities() {
        return findSucursalEntities(true, -1, -1);
    }

    public List<Sucursal> findSucursalEntities(int maxResults, int firstResult) {
        return findSucursalEntities(false, maxResults, firstResult);
    }

    private List<Sucursal> findSucursalEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Sucursal.class));
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

    public Sucursal findSucursal(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Sucursal.class, id);
        } finally {
            em.close();
        }
    }

    public int getSucursalCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Sucursal> rt = cq.from(Sucursal.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
