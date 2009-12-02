/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.chromattic;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.services.jcr.RepositoryService;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ChromatticManager
{

   /** . */
   final RepositoryService repositoryService;

   /** . */
   Map<String, String> lifeCycleToWorkspaceMap;

   /** . */
   Map<String, ChromatticLifeCycle> lifeCycles = new HashMap<String, ChromatticLifeCycle>();

   /** . */
   private static final ThreadLocal<Synchronization> currentSynchronization = new ThreadLocal<Synchronization>();

   public ChromatticManager(RepositoryService repositoryService) throws Exception {
      this.repositoryService = repositoryService;
      this.lifeCycleToWorkspaceMap = new HashMap<String, String>();
   }

   static Synchronization getCurrentSynchronization() {
      return currentSynchronization.get();
   }

   public ChromatticLifeCycle getConfigurator(String configuratorKey)
   {
      return lifeCycles.get(configuratorKey);
   }

   public void addLifeCycle(ComponentPlugin plugin)
   {
      ChromatticLifeCycle lifeCycle = (ChromatticLifeCycle)plugin;
      try
      {
         lifeCycle.manager = this;
         lifeCycle.start();
         lifeCycles.put(lifeCycle.getName(), lifeCycle);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   public Synchronization getSynchronization()
   {
      return currentSynchronization.get();
   }

   /**
    * Begins the demarcation of a request and associates the current thread of execution with
    * a context that will provides access to the correct persistence context.
    *
    * @throws IllegalStateException if a request is already associated with this thread
    */
   public void beginRequest() throws IllegalStateException
   {
      if (currentSynchronization.get() != null)
      {
         throw new IllegalStateException("Request already started");
      }

      //
      Synchronization sync = new Synchronization();

      //
      currentSynchronization.set(sync);
   }

   /**
    * Ends the demarcation of a request.
    *
    * @param save if state must be saved
    * @throws IllegalStateException if no request was started previously
    */
   public void endRequest(boolean save) throws IllegalStateException
   {
      Synchronization sync = currentSynchronization.get();

      // We set null now so it will be properly closed in the PortalSessionLifeCycle logout method
      currentSynchronization.set(null);

      //
      if (sync == null)
      {
         throw new IllegalStateException("Request not started");
      }

      // Properly close everything
      sync.close(save);

      //
      currentSynchronization.set(null);
   }
}
