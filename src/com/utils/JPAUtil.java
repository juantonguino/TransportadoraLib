/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.utils;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 *
 * @author juandiego
 */
public class JPAUtil {
    
    private static final EntityManagerFactory emf;
    
    static{
        try {
            emf=Persistence.createEntityManagerFactory("TransportadoraLibPU");
        } 
        catch (Throwable t) {
            t.printStackTrace();
            throw new ExceptionInInitializerError();
        }
    }
    
    public static EntityManagerFactory getEntityManagerFactory(){
        return emf;
    }
    
}