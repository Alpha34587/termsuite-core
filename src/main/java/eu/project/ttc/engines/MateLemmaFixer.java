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
package eu.project.ttc.engines;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import eu.project.ttc.engines.desc.Lang;
import eu.project.ttc.types.WordAnnotation;
import fr.univnantes.lina.UIMAProfiler;

/**
 * Post-process the lemma found by Mate.
 * 
 * Must be place after Stemmer AE in pipeline
 */
public class MateLemmaFixer extends JCasAnnotator_ImplBase {
	
	public static final String LANGUAGE = "Language";
	@ConfigurationParameter(name=LANGUAGE, mandatory=true)
	private String langCode;
	
	private Lang language;
	
	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		this.language = Lang.forName(langCode);
	}
	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		UIMAProfiler.getProfiler("AnalysisEngine").start(this, "process");
		FSIterator<Annotation> it = jCas.getAnnotationIndex(WordAnnotation.type).iterator();
		WordAnnotation word;
		while(it.hasNext()) {
			word = (WordAnnotation) it.next();
			if (word.getLemma() == null)
				word.setLemma(word.getCoveredText().toLowerCase(language.getLocale()));
			else if (word.getLemma().equals("CD")) //ou TermSuiteConstants.CARD_MATE
				word.setLemma(word.getCoveredText().toLowerCase(language.getLocale()));
			else  {
				word.setLemma(word.getLemma().toLowerCase());
				if (word.getLemma().equals((word.getStem()+"s"))){
				word.setLemma(word.getCoveredText().toLowerCase(language.getLocale()).replaceAll("s$", ""));
				}
			}
		}
		UIMAProfiler.getProfiler("AnalysisEngine").stop(this, "process");
	}
}
