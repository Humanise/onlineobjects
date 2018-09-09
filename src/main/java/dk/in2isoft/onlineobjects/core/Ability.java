package dk.in2isoft.onlineobjects.core;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public enum Ability {
	usePhotosApp, viewDebuggingInfo, modifyWords;
	
	public static Set<Ability> convert(Collection<String> properties) {
		return properties.stream().map((prop) -> {
			try {
				return Ability.valueOf(prop);
			} catch (IllegalArgumentException e) {
				return null;
			}
		}).filter(ability -> ability != null).collect(Collectors.toSet());
	}
}
