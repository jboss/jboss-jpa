/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.jpa.spi;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 * EJB 3 JPA 7.1.1. To create entity managers within a container
 * there is a bean installed which conforms to this interface.
 * 
 * Only one EntityManagerFactory is permitted to be created for each deployed persistence unit configuration.
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public interface PersistenceUnit
{
   /**
    * Obtain the container entity manager factory.
    * 
    * @return an EntityManagerFactory
    */
   EntityManagerFactory getContainerEntityManagerFactory();
   
   String getName();
   
   EntityManager getTransactionScopedEntityManager();
   
   /**
    * The extended persistence context resolver associated with
    * this persistence unit.
    * 
    * @return the XPC resolver
    */
   XPCResolver getXPCResolver();
   
   boolean isInTx();
   
   void verifyInTx();
}
