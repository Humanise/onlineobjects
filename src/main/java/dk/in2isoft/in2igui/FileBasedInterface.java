package dk.in2isoft.in2igui;

import java.io.File;

import dk.in2isoft.onlineobjects.ui.HUIService;

public class FileBasedInterface extends AbstractInterface {

	private File file;

	public FileBasedInterface(File file, HUIService huiService) {
		super(huiService);
		this.file = file;
	}

	@Override
	public File getFile() {
		return file;
	}

}
