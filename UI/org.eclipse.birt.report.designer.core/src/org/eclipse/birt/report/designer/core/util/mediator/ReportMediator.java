/*******************************************************************************
 * Copyright (c) 2004 Actuate Corporation .
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.report.designer.core.util.mediator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.birt.report.designer.core.util.mediator.request.ReportRequest;

/**
 * Mediator calss to control the interactive between different views.
 * This class is used for selection sychronization and other tasks.
 */
public class ReportMediator
{

	private List listeners = new ArrayList( );
	private List stack = new ArrayList( );
	private int stackPointer = 0;
	private ReportMediatorState currentState = new ReportMediatorState();

	/**
	 * Add a colleague to mediator.
	 * @param colleague
	 */
	public void addColleague( IColleague colleague )
	{
		if ( !listeners.contains( colleague ) )
		{
			listeners.add( colleague );
		}
	}

	/**
	 * Remove colleagure from mediator.
	 * @param colleague
	 */
	public void removeColleague( IColleague colleague )
	{
		listeners.remove( colleague );
	}

	/**
	 * Send a request to mediator. 
	 * Mediator handle and dispatch this request to colleaues. 
	 * @param request
	 */
	public void notifyRequest( ReportRequest request )
	{
		if (isInterestRequest(request))
		{
			currentState.copyFrom(convertRequestToState(request));
		}
		int size = listeners.size( );
		for ( int i = 0; i < size; i++ )
		{
			IColleague colleague = (IColleague) listeners.get( i );
			colleague.performRequest( request );
		}
	}

	private boolean isInterestRequest(ReportRequest request)
	{
		return ReportRequest.SELECTION.equals(request.getType());
	}
	
	/**
	 * Dispose mediator. 
	 */
	public void dispose( )
	{
		currentState = null ;
		listeners = null;
		stackPointer = 0;
		stack = null;
	}

	
	/**
	 * Return top state in stack.
	 */
	public void popState( )
	{
		stackPointer--;
		restoreState( (ReportMediatorState) stack.get( stackPointer ) );
	}

	
	/**
	 * Push state of colleague, which send the notification, into stack.
	 */
	public void pushState( )
	{
		try
		{
			ReportMediatorState s;
			if ( stack.size( ) > stackPointer )
			{
				s = (ReportMediatorState) stack.get( stackPointer );
				s.copyFrom( currentState );
			}
			else
			{
				stack.add( currentState.clone( ) );
			}
			stackPointer++;
		}
		catch ( CloneNotSupportedException e )
		{
			throw new RuntimeException( e.getMessage( ) );
		}
	}

	
	private ReportMediatorState convertRequestToState(ReportRequest request)
	{
		ReportMediatorState retValue = new ReportMediatorState();
		retValue.setSource(request.getSource());
		retValue.setSelectiobObject(request.getSelectionObject());
		return retValue;
	}
	
	private ReportRequest cconvertStateToRequest(ReportMediatorState s)
	{
		ReportRequest request = new ReportRequest();
		request.setSource(s.getSource());
		request.setSelectionObject(s.getSelectionObject());
		return request;
	}
	/**
	 * Restore previous state and discard the top one.
	 */
	public void restoreState( )
	{
		restoreState( (ReportMediatorState) stack.get( stackPointer - 1 ) );
	}

	/**
	 * Sets all State information to that of the given State, called by
	 * restoreState()
	 * 
	 * @param s
	 *            the State
	 */
	protected void restoreState( ReportMediatorState s )
	{
		currentState.copyFrom(s);
		ReportRequest request = cconvertStateToRequest(s);
		notifyRequest(request);
	}
	
	

	/** Contains the state variables of this SWTGraphics object * */
	protected static class ReportMediatorState implements Cloneable
	{

		private List SelectiobObject = new ArrayList( );
		private Object source;

		/** @see Object#clone() * */
		public Object clone( ) throws CloneNotSupportedException
		{
			ReportMediatorState state = new ReportMediatorState( );
			state.setSelectiobObject( getSelectionObject( ) );
			return state;
		}

		/**
		 * Copies all state information from the given State to this State
		 * 
		 * @param state
		 *            The State to copy from
		 */
		protected void copyFrom( ReportMediatorState state )
		{
			setSelectiobObject( state.getSelectionObject( ) );
			setSource(state.getSource());
		}

		/**
		 * @return Returns the selectiobObject.
		 */
		protected List getSelectionObject( )
		{
			return SelectiobObject;
		}

		/**
		 * @param selectiobObject
		 *            The selectiobObject to set.
		 */
		protected void setSelectiobObject( List selectiobObject )
		{
			SelectiobObject = selectiobObject;
		}

		/**
		 * @return Returns the source.
		 */
		protected Object getSource( )
		{
			return source;
		}

		/**
		 * @param source
		 *            The source to set.
		 */
		protected void setSource( Object source )
		{
			this.source = source;
		}
	}
}