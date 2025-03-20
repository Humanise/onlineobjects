package dk.in2isoft.onlineobjects.apps.knowledge.perspective;

import java.util.List;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.in2igui.data.ItemData;
import dk.in2isoft.onlineobjects.core.exceptions.BadRequestException;

public class InternetAddressEditPerspective {

	private long id;
	private String title;
	private String address;
	private List<ItemData> authors;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public void setAuthors(List<ItemData> authors) {
		this.authors = authors;
	}
	
	public List<ItemData> getAuthors() {
		return authors;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
	
	public static void validate(InternetAddressEditPerspective perspective) throws BadRequestException {
		if (perspective==null) {
			throw new BadRequestException("No data");
		}
		if (perspective.getId() < 1) {
			throw new BadRequestException("Invalid ID");
		}
		if (Strings.isBlank(perspective.getTitle())) {
			throw new BadRequestException("The title is empty");
		}
		if (Strings.isBlank(perspective.getAddress())) {
			throw new BadRequestException("The address is empty");
		}
	}
}
