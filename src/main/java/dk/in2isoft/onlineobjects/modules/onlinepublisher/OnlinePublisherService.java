package dk.in2isoft.onlineobjects.modules.onlinepublisher;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import dk.in2isoft.onlineobjects.apps.words.views.util.UrlBuilder;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.exceptions.NotFoundException;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.BadRequestException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.InternetAddress;
import dk.in2isoft.onlineobjects.model.Pile;
import dk.in2isoft.onlineobjects.modules.networking.NetworkResponse;
import dk.in2isoft.onlineobjects.modules.networking.NetworkService;
import dk.in2isoft.onlineobjects.modules.scheduling.JobStatus;
import dk.in2isoft.onlineobjects.services.PileService;

public class OnlinePublisherService {

	private ModelService modelService;
	private PileService pileService;
	private NetworkService networkService;

	public List<InternetAddress> getSites(Operator privileged) throws ModelException, SecurityException {
		Pile pile = getPile(privileged);
		Query<InternetAddress> query = Query.after(InternetAddress.class).from(pile);
		return modelService.list(query, privileged);
	}

	public void callAllPublishers(JobStatus status) {
		status.log("Performing heart massage");
		Operator operator = modelService.newPublicOperator();
		try {
			List<InternetAddress> sites = getSites(operator);
			int index = 0;
			for (InternetAddress internetAddress : sites) {
				UrlBuilder url = new UrlBuilder(internetAddress.getAddress());
				url.folder("services").folder("heartbeat");
				status.log("Calling: " + url);
				NetworkResponse response = null;
				try {
					response = networkService.get(url.toString());
					if (response.isSuccess()) {
						status.log("Success: " + url);
					} else {
						status.log("Failure: " + url);
					}
				} catch (URISyntaxException e) {
					status.error("Invalid address: " + url,e);
				} catch (IOException e) {
					status.error("Failed to request address: " + url,e);
				} finally {
					if (response!=null) {
						response.cleanUp();
					}
				}
				status.setProgress(index, sites.size());
				index++;
			}
			operator.commit();
		} catch (EndUserException e) {
			operator.rollBack();
			status.error("A problem happened in the model or secuity", e);
		}
		status.setProgress(1);
		status.log("Heart massage finished");
	}

	public void createOrUpdatePublisher(PublisherPerspective perspective, Operator privileged) throws BadRequestException, ModelException, NotFoundException, SecurityException {
		if (perspective == null) {
			throw new BadRequestException("No publisher provider");
		}
		InternetAddress address;
		if (perspective.getId() > 0) {
			address = modelService.get(InternetAddress.class, perspective.getId(), privileged);
			if (address == null) {
				throw new NotFoundException("Internet address not found (id=" + perspective.getId() + ")");
			}
		} else {
			address = new InternetAddress();
		}
		address.setAddress(perspective.getAddress());
		address.setName(perspective.getName());
		modelService.createOrUpdate(address, privileged);
		Pile pile = getPile(privileged);
		if (!modelService.getRelation(pile, address,privileged).isPresent()) {
			modelService.createRelation(pile, address, privileged);
		}
	}

	public PublisherPerspective getPublisherPerspective(Long id, Operator privileged) throws ModelException, NotFoundException {
		InternetAddress internetAddress = modelService.get(InternetAddress.class, id, privileged);
		if (internetAddress!=null) {
			PublisherPerspective perspective = new PublisherPerspective();
			perspective.setId(internetAddress.getId());
			perspective.setAddress(internetAddress.getAddress());
			perspective.setName(internetAddress.getName());
			return perspective;
		}
		throw new NotFoundException("The internet address does not exists: id="+id);
	}

	public void deletePublisher(Long id, Operator privileged) throws ModelException, SecurityException {
		InternetAddress internetAddress = modelService.get(InternetAddress.class, id, privileged);
		if (internetAddress!=null) {
			modelService.delete(internetAddress, privileged);
		}
	}

	private Pile getPile(Operator privileged) throws ModelException, SecurityException {
		return pileService.getOrCreateGlobalPile("onlinepublisher.sites", privileged);
	}
	
	// Wiring...

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	public void setPileService(PileService pileService) {
		this.pileService = pileService;
	}

	public void setNetworkService(NetworkService networkService) {
		this.networkService = networkService;
	}
}
