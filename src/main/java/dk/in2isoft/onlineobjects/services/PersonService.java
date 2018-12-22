package dk.in2isoft.onlineobjects.services;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Period;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.core.EntitylistSynchronizer;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Address;
import dk.in2isoft.onlineobjects.model.EmailAddress;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.InternetAddress;
import dk.in2isoft.onlineobjects.model.Person;
import dk.in2isoft.onlineobjects.model.PhoneNumber;
import dk.in2isoft.onlineobjects.model.Property;
import dk.in2isoft.onlineobjects.modules.user.UserProfileInfo;

public class PersonService {

	private ModelService modelService;
		
	public Address getPersonsPreferredAddress(Person person, Privileged privileged) throws ModelException {
		return modelService.getChild(person, Property.KEY_COMMON_PREFERRED, Address.class, privileged);
	}
	
	public Person getOrCreatePerson(String text, Privileged privileged) throws ModelException, SecurityException {
		if (Strings.isBlank(text) || privileged == null) {
			return null;
		}
		text = text.replaceAll("[\\s]+", " ").trim();
		Query<Person> query = Query.after(Person.class).withName(text).as(privileged);
		Person person = modelService.getFirst(query);
		if (person==null) {
			person = new Person();
			person.setFullName(text);
			modelService.create(person, privileged);
		}
		return person;
	}
	
	public String getFullPersonName(Person person, int maxLength) {
		String fullName = person.getFullName();
		String given = person.getGivenName();
		String givenFirst = abbreviate(given);
		String family = person.getFamilyName();
		String additional = person.getAdditionalName();
		String additionalFirst = abbreviate(additional);
		if (fullName.length()>maxLength) {

			String givenAdditionFirstFamily = Strings.concatWords(given, additionalFirst, family);
			if (givenAdditionFirstFamily.length()<=maxLength) {
				return givenAdditionFirstFamily;
			}
			
			String givenFamily = Strings.concatWords(given, family);
			if (givenFamily.length()<=maxLength) {
				return givenFamily;
			}
			
			String givenFirstFamily = Strings.concatWords(givenFirst, family);
			if (givenFirstFamily.length()<=maxLength) {
				return givenFirstFamily;
			}
		}
		return StringUtils.abbreviate(fullName,maxLength);
	}

	private String abbreviate(String name) {
		if (Strings.isBlank(name)) {
			return null;
		}
		return name.trim().substring(0, 1).toUpperCase()+".";
	}
	
	public void updatePersonsPreferredAddress(Person person, Address address, Privileged privileged) throws ModelException, SecurityException {
		Address existing = getPersonsPreferredAddress(person, privileged);
		if (existing!=null) {
			existing.setStreet(address.getStreet());
			existing.setCity(address.getCity());
			existing.setRegion(address.getRegion());
			existing.setPostalCode(address.getPostalCode());
			existing.setCountry(address.getCountry());
			modelService.update(existing, privileged);
		} else {
			modelService.create(address, privileged);
			modelService.createRelation(person, address, Property.KEY_COMMON_PREFERRED, privileged);
		}
	}

	public UserProfileInfo getProfileInfo(Person person,Privileged priviledged) throws ModelException {
		UserProfileInfo info = new UserProfileInfo();
		info.setGivenName(person.getGivenName());
		info.setFamilyName(person.getFamilyName());
		info.setAdditionalName(person.getAdditionalName());
		info.setSex(person.getSex());
		info.setResume(person.getPropertyValue(Property.KEY_HUMAN_RESUME));
		info.setInterests(person.getPropertyValues(Property.KEY_HUMAN_INTEREST));
		info.setMusic(person.getPropertyValues(Property.KEY_HUMAN_FAVORITE_MUSIC));
		info.setMovies(person.getPropertyValues(Property.KEY_HUMAN_FAVORITE_MOVIE));
		info.setBooks(person.getPropertyValues(Property.KEY_HUMAN_FAVORITE_BOOK));
		info.setTelevisionPrograms(person.getPropertyValues(Property.KEY_HUMAN_FAVORITE_TELEVISIONPROGRAM));
		info.setEmails(modelService.getChildren(person, EmailAddress.class,priviledged));
		info.setPhones(modelService.getChildren(person, PhoneNumber.class,priviledged));
		info.setUrls(modelService.getChildren(person, InternetAddress.class,priviledged));
		return info;
	}
	
	public void updateDummyEmailAddresses(Entity parent,List<EmailAddress> addresses, Privileged session) throws EndUserException {
		
		// Remove empty addresses
		for (Iterator<EmailAddress> i = addresses.iterator(); i.hasNext();) {
			EmailAddress emailAddress = i.next();
			if (!Strings.isNotBlank(emailAddress.getAddress())) {
				i.remove();
			}
		}
		
		List<EmailAddress> existing = modelService.getChildren(parent, EmailAddress.class, session);
		EntitylistSynchronizer<EmailAddress> sync = new EntitylistSynchronizer<EmailAddress>(existing,addresses);
		
		for (Entry<EmailAddress, EmailAddress> entry : sync.getUpdated().entrySet()) {
			EmailAddress original = entry.getKey();
			EmailAddress dummy = entry.getValue();
			original.setAddress(dummy.getAddress());
			original.setContext(dummy.getContext());
		}
		
		for (EmailAddress emailAddress : sync.getNew()) {
			modelService.create(emailAddress, session);
			modelService.createRelation(parent, emailAddress, session);
		}
		
		for (EmailAddress emailAddress : sync.getDeleted()) {
			modelService.delete(emailAddress, session);
		}
	}

	
	public void updateDummyPhoneNumbers(Entity parent,List<PhoneNumber> phones, Privileged priviledged) throws EndUserException {

		// Remove empty addresses
		for (Iterator<PhoneNumber> i = phones.iterator(); i.hasNext();) {
			PhoneNumber number = i.next();
			if (!Strings.isNotBlank(number.getNumber())) {
				i.remove();
			}
		}
		List<PhoneNumber> existing = modelService.getChildren(parent, PhoneNumber.class, priviledged);
		EntitylistSynchronizer<PhoneNumber> sync = new EntitylistSynchronizer<PhoneNumber>(existing,phones);
		
		for (Entry<PhoneNumber, PhoneNumber> entry : sync.getUpdated().entrySet()) {
			PhoneNumber original = entry.getKey();
			PhoneNumber dummy = entry.getValue();
			original.setNumber(dummy.getNumber());
			original.setContext(dummy.getContext());
		}
		
		for (PhoneNumber number : sync.getNew()) {
			modelService.create(number, priviledged);
			modelService.createRelation(parent, number, priviledged);
		}
		
		for (PhoneNumber number : sync.getDeleted()) {
			modelService.delete(number, priviledged);
		}
	}
	
	public Integer getYearsOld(Person person) {
		if (person==null || person.getBirthday()==null) {
			return null;
		}
		DateTime now = new DateTime();
		Period period = new Period(new DateTime(person.getBirthday()),now);
		return period.getYears();
	}
	
	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
}
