package dk.in2isoft.onlineobjects.apps.tools;

import java.util.List;

import dk.in2isoft.onlineobjects.model.EmailAddress;
import dk.in2isoft.onlineobjects.model.Person;
import dk.in2isoft.onlineobjects.model.PhoneNumber;

public class PersonPerspective {
	private Person person;
	private List<EmailAddress> emails;
	private List<PhoneNumber> phones;

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public List<EmailAddress> getEmails() {
		return emails;
	}

	public void setEmails(List<EmailAddress> emails) {
		this.emails = emails;
	}

	public List<PhoneNumber> getPhones() {
		return phones;
	}

	public void setPhones(List<PhoneNumber> phones) {
		this.phones = phones;
	}
}
