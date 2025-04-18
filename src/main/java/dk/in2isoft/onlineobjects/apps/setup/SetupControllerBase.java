package dk.in2isoft.onlineobjects.apps.setup;

import java.util.List;
import java.util.Locale;

import dk.in2isoft.onlineobjects.apps.ApplicationController;
import dk.in2isoft.onlineobjects.apps.words.LoadManager;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.modules.caching.CacheService;
import dk.in2isoft.onlineobjects.modules.index.IndexService;
import dk.in2isoft.onlineobjects.modules.localization.LocalizationService;
import dk.in2isoft.onlineobjects.modules.onlinepublisher.OnlinePublisherService;
import dk.in2isoft.onlineobjects.modules.scheduling.SchedulingService;
import dk.in2isoft.onlineobjects.modules.surveillance.SurveillanceService;
import dk.in2isoft.onlineobjects.modules.user.MemberService;
import dk.in2isoft.onlineobjects.services.PasswordRecoveryService;
import dk.in2isoft.onlineobjects.ui.Request;

public abstract class SetupControllerBase extends ApplicationController {

	protected SecurityService securityService;
	protected SchedulingService schedulingService;
	protected SurveillanceService surveillanceService;
	protected LocalizationService localizationService;
	protected OnlinePublisherService onlinePublisherService;
	protected MemberService memberService;
	protected IndexService indexService;
	protected PasswordRecoveryService passwordRecoveryService;
	protected CacheService cacheService;
	protected LoadManager wordsLoadManager;

	public SetupControllerBase() {
		super("setup");
	}

	public List<Locale> getLocales() {
		return null;
	}

	@Override
	public boolean isAllowed(Request request) {
		return securityService.isAdminUser(request.getSession());
	}
	
	@Override
	public boolean askForUserChange(Request request) {
		return request.isLocalRoot();
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public void setSchedulingService(SchedulingService schedulingService) {
		this.schedulingService = schedulingService;
	}

	public void setSurveillanceService(SurveillanceService surveillanceService) {
		this.surveillanceService = surveillanceService;
	}

	public void setLocalizationService(LocalizationService localizationService) {
		this.localizationService = localizationService;
	}
	
	public void setOnlinePublisherService(OnlinePublisherService onlinePublisherService) {
		this.onlinePublisherService = onlinePublisherService;
	}
	
	public void setIndexService(IndexService indexService) {
		this.indexService = indexService;
	}
	
	public void setMemberService(MemberService memberService) {
		this.memberService = memberService;
	}
	
	public void setPasswordRecoveryService(PasswordRecoveryService passwordRecoveryService) {
		this.passwordRecoveryService = passwordRecoveryService;
	}
	
	public void setCacheService(CacheService cacheService) {
		this.cacheService = cacheService;
	}
	
	public void setWordsLoadManager(LoadManager loadManager) {
		this.wordsLoadManager = loadManager;
	}
}