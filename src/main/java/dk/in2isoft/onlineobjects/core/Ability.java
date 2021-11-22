package dk.in2isoft.onlineobjects.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public enum Ability {
	usePhotosApp, viewDebuggingInfo, modifyWords, earlyAdopter;
	
	public static Set<Ability> convert(Collection<String> properties) {
		Set<Ability> converted = new HashSet<>();
		for (String property : properties) {
			for (Ability ability : Ability.values()) {
				if (ability.name().equals(property)) {
					converted.add(ability);
				}
			}
		}
		return converted;
	}
}
