/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jpa.deployers.switchboard;

import java.util.Collection;

import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContextType;

import org.jboss.jpa.deployment.ManagedEntityManagerFactory;
import org.jboss.jpa.deployment.PersistenceUnitDeployment;
import org.jboss.jpa.spi.PersistenceUnit;
import org.jboss.jpa.spi.PersistenceUnitRegistry;
import org.jboss.jpa.tx.TransactionScopedEntityManager;
import org.jboss.logging.Logger;
import org.jboss.switchboard.javaee.environment.PersistenceContextRefType;
import org.jboss.switchboard.spi.Resource;



/**
 * The PersistenceContext (PC) for a bean, is the EntityManager (EM) that
 * will be used to access it.  Think of potentially, separate EM (different name) per bean
 * in the transaction but possibly the same Database connection shared for
 * each EM (via EE sharing of managed connections).  Or same EM (same name) for each bean
 * in the transaction.  The third case, would be separate EM (different name) per bean
 * in the transaction and different resource managers (multiple DB servers).
 *
 * The Extended PersistenceContext (XPC) has the same capabilities as above, with an extended lifetime that
 * is potentially long term.  Work done outside of a transaction, is queued up until the next transaction is
 * committed or rolled back.  Work done inside of a transaction is committed like a regular PC would do.
 *
 * @author Scott Marlow
 */
public class PersistenceContextRefResource implements Resource
{
   private static final Logger log = Logger.getLogger(PersistenceContextRefResource.class);
   private final String puSupplier;
   private final PersistenceContextRefType pcRef;

   public PersistenceContextRefResource(String puSupplier, PersistenceContextRefType pcRef)
   {
      if (puSupplier == null)
      {
         throw new IllegalArgumentException("Cannot create a PersistenceUnitRefResource for a null persistence unit supplier");
      }
      this.puSupplier = puSupplier;
      this.pcRef = pcRef;
   }

   @Override
   public Object getDependency()
   {
      // We need the PersistenceUnitDeployer MC bean to be started before we can bind
      // the PersistenceUnitDeployment.getManagedFactory().getEntityManagerFactory() to JNDI
      return puSupplier;
   }

   /**
    * @return an object that can be bound that represents the target PersistentContext.
    */
   @Override
   public Object getTarget()
   {
      boolean extendedPc = PersistenceContextType.EXTENDED.equals(pcRef.getPersistenceContextType());

      Object target;
      if (extendedPc)
      {
         Reference reference = new Reference(EntityManager.class.getName(), ExtendedEntityManagerObjectFactory.class.getName(), null);
         reference.add(new StringRefAddr("puName", puSupplier));
         target = reference;
      }
      else
      {
         // get errors here rather than later when objectfactory is used.
         PersistenceUnit pu = PersistenceUnitRegistry.getPersistenceUnit(puSupplier);
         if (pu == null)
         {
            // keep the error message in sync with PersistenceUnitHandler.ERROR_MESSAGE_FAILED_TO_RESOVLE_PU
            throw new RuntimeException("failed to resolve persistence unit " + puSupplier);
         }
         ManagedEntityManagerFactory factory = ((PersistenceUnitDeployment)pu).getManagedFactory();
         if (factory == null)  // check for error here
         {
            throw new RuntimeException("could not find EntityManagerFactory for persistenceUnit " + puSupplier);
         }

         Reference reference = new Reference(EntityManager.class.getName(), EntityManagerObjectFactory.class.getName(), null);
         reference.add(new StringRefAddr("puName", puSupplier));
         target = reference;
      }
      if(log.isTraceEnabled())  
         log.trace("returning target = " + target);
      return target;
   }

   @Override
   public String toString()
   {
      boolean extendedPc = PersistenceContextType.EXTENDED.equals(pcRef.getPersistenceContextType());
      return PersistenceContextRefResource.class.getSimpleName() + (extendedPc?"(extendedPC)":"")+"[supplier=" + this.puSupplier + "]";
   }
   
   @Override
   public Collection<?> getInvocationDependencies()
   {
      return null;
   }

}
