/*******************************************************************************
 * Copyright 2015 - CNRS (Centre National de Recherche Scientifique)
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *******************************************************************************/
package eu.project.ttc.resources;

import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.SharedResourceObject;

import eu.project.ttc.models.TermIndex;

public class TermIndexResource implements SharedResourceObject {
	/**
	 * The name of the Term Index as UIMA resource
	 */
	public static final String TERM_INDEX = "TermIndex";
	
	
	private TermIndex termIndex;
	
	public void load(DataResource arg0) throws ResourceInitializationException {
		this.termIndex = MemoryTermIndexManager.getInstance().getIndex(arg0.getUri().toString());
	};
	
	public TermIndex getTermIndex() {
		return this.termIndex;
	}
}
