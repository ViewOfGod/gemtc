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

import java.util.Arrays;

import org.drugis.mtc.model.Measurement;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("unused")
public class DichotomousNetworkBuilderTest {
	private Study study(String id, Measurement[] m) {
		Study study = new Study(id);
		study.getMeasurements().addAll(Arrays.asList(m));
		return study;
	}

	private DichotomousNetworkBuilder<String> d_builder;
	private Treatment d_ta = new Treatment("A");
	private Treatment d_tb = new Treatment("B");
	private Treatment d_tc = new Treatment("C");
	private Measurement d_s1a = new Measurement(d_ta, 5, 100);
	private Measurement d_s1b = new Measurement(d_tb, 23, 100);
	private Study d_s1 = study("1", new Measurement[]{d_s1a, d_s1b});
	private Measurement d_s2b = new Measurement(d_tb, 12, 43);
	private Measurement d_s2c = new Measurement(d_tc, 15, 40);
	private Study d_s2 = study("2", new Measurement[]{d_s2b, d_s2c});

	@Before 
	public void setUp() {
		d_builder = new DichotomousNetworkBuilder<String>();
	}

	@Test 
	public void testBuild() {
		d_builder.add("1", "A", d_s1a.getResponders(), d_s1a.getSampleSize());
		d_builder.add("1", "B", d_s1b.getResponders(), d_s1b.getSampleSize());
		d_builder.add("2", "B", d_s2b.getResponders(), d_s2b.getSampleSize());
		d_builder.add("2", "C", d_s2c.getResponders(), d_s2c.getSampleSize());
		Network n = d_builder.buildNetwork();

		// TODO: test measurement contents
	}
}
