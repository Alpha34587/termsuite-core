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
package eu.project.ttc.models;

import com.google.common.base.Objects;

public class TermWord {
	private Word word;
	private String syntacticlabel;
	
	TermWord(Word word, String label) {
		super();
		this.word = word;
		this.syntacticlabel = label;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TermWord) {
			TermWord o = (TermWord) obj;
			return Objects.equal(word, o.word) && Objects.equal(syntacticlabel, o.syntacticlabel) ;
		} else
			return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(this.word, this.syntacticlabel);
	}
	
	public String getSyntacticLabel() {
		return syntacticlabel;
	}
	public Word getWord() {
		return word;
	}
	

	@Override
	public String toString() {
		return syntacticlabel + ":" + word.getLemma();
	}
}
