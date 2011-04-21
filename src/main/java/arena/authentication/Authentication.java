/*
 * Keystone Development Framework
 * Copyright (C) 2004-2009 Rick Knowles
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * Version 2 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License Version 2 for more details.
 *
 * You should have received a copy of the GNU Library General Public License
 * Version 2 along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package arena.authentication;

import arena.action.RequestState;

/**
 * Defines an implementation of how the browser and server can negotiate an 
 * authenticated user. Options include simple form based authentication, NTLM
 * user negotiation or custom implmentations might read pre-defined cookies, etc.
 * 
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id$
 * @param <T> the type of user this object will authenticate
 */
public interface Authentication<T> {

    public boolean isLoggedIn(RequestState requestState);
    
    public T getLoggedInUser(RequestState requestState);

    public void setLoggedInUser(RequestState requestState, T loggedInUser);

    public void logout(RequestState requestState);
}