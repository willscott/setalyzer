package com.quimian.setalyzer;

import java.util.ArrayList;
import java.util.List;

import com.quimian.setalyzer.util.SetCard;

public class SetFinder {
	public static List<List<SetCard>> findSets(List<SetCard> cards) {
		List<List<SetCard>> sets = new ArrayList<List<SetCard>>();
		
		for (SetCard a : cards) {
			for (SetCard b : cards) {
				if (a == b)
					continue;
				for (SetCard c : cards) {
					if (a == c || b == c)
						continue;
					
					if ((alike(a.color, b.color, c.color) || different(a.color, b.color, c.color)) &&
							(alike(a.count, b.count, c.count) || different(a.count, b.count, c.count)) &&
							(alike(a.shade, b.shade, c.shade) || different(a.shade, b.shade, c.shade)) &&
							(alike(a.shape, b.shape, c.shape) || different(a.shape, b.shape, c.shape))) {
						ArrayList<SetCard> set = new ArrayList<SetCard>();
						set.add(a);
						set.add(b);
						set.add(c);
						sets.add(set);
					}
				}
			}
		}
		
		return sets;
	}
	
	public static boolean alike(Object a, Object b, Object c) {
		return a == b && b == c;
	}
	public static boolean different(Object a, Object b, Object c) {
		return a != b && b != c && a != c;
	}
}
