/*
 * This file is part of drugis.org MTC.
 * MTC is distributed from http://drugis.org/mtc.
 * Copyright (C) 2009-2011 Gert van Valkenhoef.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.drugis.mtc;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;
import org.apache.commons.collections15.bidimap.UnmodifiableBidiMap;
import org.drugis.common.EqualsUtil;
import org.drugis.mtc.model.Measurement;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;

public class NetworkBuilder<TreatmentType> {
	private static final Pattern s_treatmentIdPattern = Pattern.compile("^[A-Za-z0-9_]+$");
	
	public static class ToStringTransformer<T> implements Transformer<T, String> {
		public String transform(T input) {
			return input.toString();
		}
	}

	private static class MKey {
		public final String studyId;
		public final Treatment treatment;
		
		public MKey(String studyId, Treatment treatment) {
			this.studyId = studyId;
			this.treatment = treatment;
		}
		
		@Override
		public boolean equals(Object o) {
			if (o != null && o instanceof MKey) {
				MKey other = (MKey) o;
				return EqualsUtil.equal(studyId, other.studyId) && EqualsUtil.equal(treatment, other.treatment);
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return studyId.hashCode() * 31 + treatment.hashCode();
		}
	}

	private BidiMap<TreatmentType, Treatment> d_treatmentMap = new DualHashBidiMap<TreatmentType, Treatment>();
	private Map<MKey, Measurement> d_measurementMap = new HashMap<MKey, Measurement>();
	private Transformer<TreatmentType, String> d_idToString;

	public NetworkBuilder() {
		this(new ToStringTransformer<TreatmentType>());
	}
	
	public NetworkBuilder(Transformer<TreatmentType, String> idToString) {
		d_idToString = idToString;
	}
	
	public Network buildNetwork() {
		final Network network = new Network();
		network.getTreatments().addAll(getTreatments());
		network.getStudies().addAll(getStudies());
		return network;
	}
	
	public BidiMap<TreatmentType, Treatment> getTreatmentMap() {
		return UnmodifiableBidiMap.decorate(d_treatmentMap);
	}
	
	protected void add(String studyId, Treatment t, Measurement measurement) {
		MKey key = new MKey(studyId, t);
		if (d_measurementMap.containsKey(key)) {
			throw new IllegalArgumentException("Study/Treatment combination already mapped.");
		}
		d_measurementMap.put(key, measurement);
	}

	protected Treatment makeTreatment(TreatmentType id) {
		if (!d_treatmentMap.containsKey(id)) {
			d_treatmentMap.put(id, new Treatment(createId(id)));
		}
		return d_treatmentMap.get(id);
	}

	private String createId(TreatmentType id) {
		String transformed = d_idToString.transform(id);
		Matcher matcher = s_treatmentIdPattern.matcher(transformed);
		if (matcher.matches()) {
			return transformed;
		} else {
			throw new IllegalArgumentException("Illegal Treatment id: " + transformed);
		}
	}

	private Set<Study> getStudies() {
		Set<String> ids = new HashSet<String>();
		for (MKey key : d_measurementMap.keySet()) {
			ids.add(key.studyId);
		}
		Set<Study> studies = new HashSet<Study>();
		for (String id : ids) {
			studies.add(getStudy(id));
		}
		return studies;
	}

	private Study getStudy(String id) {
		Map<Treatment, Measurement> measurements = new HashMap<Treatment, Measurement>();
		for (MKey key : d_measurementMap.keySet()) {
			if (key.studyId.equals(id)) {
				measurements.put(key.treatment, d_measurementMap.get(key));
			}
		}
		final Study study = new Study(id);
		study.getMeasurements().addAll(measurements.values());
		return study;
	}

	private Collection<Treatment> getTreatments() {
		return d_treatmentMap.values();
	}
}
