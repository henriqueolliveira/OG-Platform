/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user.rest;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.UriInfo;

/**
 * Temporary RESTful resource representing a collection of a user's clients. If any client is requested then it will
 * magically exist and persist forever.
 * 
 * This is where clients would be deleted if heartbeats stop, automatically clearing out all client-related state. 
 */
@Path("/data/users/{username}/clients")
public class ClientsResource {

  private final UserResource _userResource;
  private final UsersResourceContext _context;
  private final ConcurrentHashMap<String, ClientResource> _clientMap = new ConcurrentHashMap<String, ClientResource>();
  
  public ClientsResource(final UserResource userResource, final UsersResourceContext context) {
    _userResource = userResource;
    _context = context;
  }
  
  public UserResource getUserResource() {
    return _userResource;
  }
  
  /**
   * Gets the URI info.
   * @return the uri info, not null
   */
  public UriInfo getUriInfo() {
    return getUserResource().getUriInfo();
  }
  
  @Path("{clientUid}")
  public ClientResource getClient(@PathParam("clientUid") String clientName) {
    ClientResource freshClient = new ClientResource(this, clientName, _context);
    ClientResource actualClient = _clientMap.putIfAbsent(clientName, freshClient);
    if (actualClient == null) {
      actualClient = freshClient;
    }
    return actualClient;
  }
  
  /**
   * Temporary method to get all clients. This should be accessible from an underlying UserMaster, when it exists.
   * 
   * @return  a collection of all clients, unmodifiable, not null
   */
  public Collection<ClientResource> getAllClients() {
    return Collections.unmodifiableCollection(_clientMap.values());
  }
  
}