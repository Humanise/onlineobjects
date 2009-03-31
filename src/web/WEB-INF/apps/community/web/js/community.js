if (!oo) var oo = {};
if (!oo.community) oo.community = {};

oo.community.Chrome = function() {
	this.addBehavior();
	new oo.community.Chrome.Login();
	new oo.community.Chrome.SignUp();
	new oo.community.Chrome.UserInfo();
	new oo.community.SearchField({element:'chrome_search'});
}

oo.community.Chrome.get = function() {
	var c = oo.community.Chrome;
	if (!c.instance) {
		c.instance = new c();
	}
	return c.instance;
}

oo.community.Chrome.prototype = {
	addBehavior : function() {
		var self = this;
		var logout = $('logOut');
		if (logout) {
			logout.onclick = function() {
				self.logOut();
				return false;
			}
		}
		$$('ul.navigation a').each(function(node) {
			node.observe('click',function() {
				self.udpateNavigation(this);
			});
		});
	},
	logOut : function() {
		CoreSecurity.logOut(function() {
			In2iGui.fadeOut($$('.login_info')[0],1000);
			In2iGui.showMessage('Du er nu logget ud');
			window.setTimeout(function() {
				In2iGui.hideMessage();
			},2000);
		});
	},
	udpateNavigation : function(element) {
		$$('ul.navigation a').each(function(node) {
			node.removeClassName('selected');
		});
		element.addClassName('selected');
	}
}

oo.community.Chrome.buildUserWebsiteURL = function(username) {
	return 'http://'+oo.baseDomainContext+'/'+username+'/site/';
	return oo.domainIsIP
		? 'http://'+oo.baseDomainContext+'/'+username+'/site/'
		: 'http://'+username+'.'+oo.baseDomainContext+'/';
}

oo.community.Chrome.buildUserProfileURL = function(username) {
	return oo.appContext+'/'+username+'/';
}

///////////////////////// Search field ///////////////////

oo.community.SearchField = function(o) {
	var e = this.element = $(o.element);
	this.name = o.name;
	this.input = e.select('input')[0];
	this.reset = e.select('.reset')[0];
	this.field = new In2iGui.TextField(this.input);
	this.field.addDelegate(this);
	this.result = $('chrome_search_result');
	this.busy = false;
	this.expanded = false;
	In2iGui.extend(this);
	this.addBehavior();
}

oo.community.SearchField.prototype = {
	addBehavior : function() {
		this.input.observe('focus',function() {
			this.animateInput(true);
		}.bind(this)).observe('blur',function() {
			this.animateInput(false);
		}.bind(this));
		this.reset.observe('click',function(e) {
			e.stop();
			this.field.setValue('');
			this.valueChanged('');
		}.bind(this));
	},
	animateInput : function(expand) {
		n2i.ani(this.input,'width',expand ? '185px' : '100px',500,{ease:n2i.ease.slowFastSlow});
	},
	valueChanged : function(value) {
		this.dirty = value.length>0;
		this.element.setClassName('chrome_search_dirty',this.dirty);
		var c = $$('.content')[0];
		var r = $$('.content_right')[0];
		var b = $$('.content_right_body')[0];
		if (this.dirty) {
			r.style.height=c.clientHeight+'px';
			this.result.style.height = c.clientHeight+'px';
			this.search(value);
		} else {
			$$('.content_right')[0].removeClassName('content_right_busy');
			r.style.height='';
			this.result.hide();
			b.show();
		}
		n2i.ani(b,'opacity',this.dirty ? 0 : 1,500,{ease:n2i.ease.slowFastSlow,onComplete:function() {
			b.style.display = this.dirty ? 'none' : '';
			this.expanded = this.dirty;
			this.checkWaitingResult();
		}.bind(this)});
	},
	search : function(query) {
		if (!this.dirty) return;
		if (this.busy) {
			this.waitingQuery=query;
			return;
		}
		this.waitingQuery = null;
		this.busy = true;
		$$('.content_right')[0].addClassName('content_right_busy');
		AppCommunity.getLatest(query,function(map) {
			this.updateList(map);
			this.busy = false;
			$$('.content_right')[0].removeClassName('content_right_busy');
			if (this.waitingQuery) {
				this.search(this.waitingQuery);
			}
		}.bind(this));
	},
	updateList : function(result) {
		if (!this.dirty) return;
		if (!this.expanded) {
			this.waitingResult = result;
			return;
		}
		this.result.style.display='block';
		this.result.update(this.buildUsers(result.users));
		this.waitingResult = null;
	},
	checkWaitingResult : function() {
		if (this.waitingResult) this.updateList(this.waitingResult);
	},
	buildUsers : function(users) {
		var html = '<div class="chrome_result_group"><h2>Brugere</h2><ul>';
		users.each(function(entry) {
			html+='<li class="user">'+
			'<div class="thumbnail"></div>'+
			'<p class="name"><a href="'+oo.community.Chrome.buildUserProfileURL(entry.user.username)+'" class="link"><span>'+entry.person.fullName+'</span></a></p>'+
			'<p class="username">'+entry.user.username+'</p>'+
			'<p class="website"><a href="'+oo.community.Chrome.buildUserWebsiteURL(entry.user.username)+'" class="link"><span>Website »</span></a></p>'+
			'</li>';
		});
		html+= '</ul></div>';
		return html;
	}
}


oo.community.Chrome.UserInfo = function() {
	this.base = $('userinfo');
	if (!this.base) return;
	this.addBeahvior();
}

oo.community.Chrome.UserInfo.prototype = {
	addBeahvior : function() {
		this.base.select('a.logout')[0].observe('click',this.logOut.bind(this));
	},
	logOut : function(e) {
		e.stop();
		CoreSecurity.logOut(function() {
			In2iGui.showMessage('Du er nu logget ud');
			window.setTimeout(function() {
				document.location.reload();
			},1000);
		});
	}
}

/**************************************** Log in handler *************************************/

oo.community.Chrome.Login = function() {
	this.form = $('login');
	if (!this.form) return;
	this.username = new In2iGui.TextField(this.form.username,null,{placeholderElement:this.form.select('label')[0]});
	this.password = new In2iGui.TextField(this.form.password,null,{placeholderElement:this.form.select('label')[1]});
	this.addBeahvior();
}

oo.community.Chrome.Login.prototype = {
	addBeahvior : function() {
		var self = this;		
		this.form.onsubmit = function() {
			if (!n2i.browser.gecko && !n2i.browser.webkit && !n2i.browser.msie7) {
				In2iGui.get().alert({
					title:'Den webbrowser De anvender er ikke understøttet.',
					text:''+
					'De kan anvende enten Internet Explorer 7, Firefox 2+ eller Safari 3+.',
					emotion:'gasp'
				});
				return false;
			}
			var valid = true;
			if (self.username.isBlank()) {
				self.username.setError('Skal udfyldes');
				self.username.focus();
				valid = false;
			} else {
				self.username.setError(false);
			}
			if (self.password.isBlank()) {
				self.password.setError('Skal udfyldes');
				valid = false;
			} else {
				self.password.setError(false);
			}
			if (!valid) return false;
			var username = self.username.getValue();
			var password = self.password.getValue();
			var delegate = {
	  			callback:function(data) {
					if (data==true) {
						self.userDidLogIn(username);
					} else {
						In2iGui.hideMessage();
						self.username.setError('Kunne ikke logge ind!')
					}
				},
	  			errorHandler:function(errorString, exception) {  }
			};
			In2iGui.showMessage('Logger ind...');
			CoreSecurity.changeUser(username,password,delegate);
			return false;
		}
		this.form.select('.sidebar_button')[0].onclick = function() {self.form.onsubmit();return false;};
		this.form.select('.submit')[0].tabIndex=-1;
	},
	userDidLogIn : function(username) {
		/*In2iGui.get().alert({
			emotion: 'smile',
			title: 'Du er nu logget ind!',
			text: '...og vil blive taget til din profil-side med det samme.'
		});*/
		In2iGui.showMessage('Login lykkedes!');
		window.setTimeout(function() {
			document.location=oo.community.Chrome.buildUserProfileURL(username);
		},500);
	}
}

/**************************************** Sign up handler *************************************/

oo.community.Chrome.SignUp = function() {
	this.form = $('signup');
	var labels = this.form.select('label');
	this.username = new In2iGui.TextField(this.form.abc,null,{placeholderElement:labels[0]});
	this.password = new In2iGui.TextField(this.form.def,null,{placeholderElement:labels[1]});
	this.name = new In2iGui.TextField(this.form.name,null,{placeholderElement:labels[2]});
	this.email = new In2iGui.TextField(this.form.email,null,{placeholderElement:labels[3]});
	this.addBehavior();
}

oo.community.Chrome.SignUp.prototype = {
	addBehavior : function() {
		var self = this;
		this.form.onsubmit=function() {
			self.submit();
			return false;
		};
		this.form.select('.sidebar_button')[0].onclick = function() {self.form.onsubmit();return false;};
		this.form.select('.submit')[0].tabIndex=-1;
	},
	submit : function() {
		if (!n2i.browser.gecko && !n2i.browser.webkit && !n2i.browser.msie7) {
			In2iGui.get().alert({
				title:'Den webbrowser De anvender er ikke understøttet.',
				text:''+
				'De kan anvende enten Internet Explorer 7, Firefox 2+ eller Safari 3+.',
				emotion:'gasp'
			});
			return false;
		}
		var username = this.username.getValue();
		var password = this.password.getValue();
		var name = this.name.getValue();
		var email = this.email.getValue();
		var valid = true;
		if (this.username.isEmpty()) {
			valid = false;
			this.username.setError('Skal udfyldes');
		} else {
			this.username.setError(false);
		}
		if (this.password.isEmpty()) {
			valid = false;
			this.password.setError('Skal udfyldes');
		} else {
			this.password.setError(false);
		}
		if (this.name.isEmpty()) {
			valid = false;
			this.name.setError('Skal udfyldes');
		} else {
			this.name.setError(false);
		}
		if (this.email.isEmpty()) {
			valid = false;
			this.email.setError('Skal udfyldes');
		} else {
			this.email.setError(false);
		}
		if (!valid) {
			return false;
		}
		var self = this;
		AppCommunity.signUp(username,password,name,email,{
			callback :function() { self.userDidSignUp(username) },
			errorHandler :function(msg,e) { self.handleFailure(e) }
		});
		return false;
	},
	handleFailure : function(e) {
		if (e.code=='userExists') {
			this.username.setError('Navnet er optaget');
		} else if (e.code=='invalidUsername' || e.code=='noUsername') {
			this.username.setError('Navnet er ikke validt');
		} else if (e.code=='noName') {
			this.name.setError('Navnet er ikke validt');
		} else if (e.code=='invalidEmail' || e.code=='noEmail') {
			this.email.setError('Adressen er ikke valid');
		} else if (e.code=='noPassword') {
			this.password.setError('Kodeordet er ikke validt');
		}
	},
	userDidSignUp : function(username) {
		this.username.setValue();
		this.password.setValue();
		this.name.setValue();
		this.email.setValue();
		In2iGui.showMessage('Opretter hjemmeside...');
		window.setTimeout(function() {
			document.location=oo.community.Chrome.buildUserWebsiteURL(username)+'?edit=true&firstRun=true'
		},2000);
		return;
		In2iGui.get().alert({
			emotion: 'smile',
			title: 'Du er nu oprettet som bruger...',
			text: '...og der er oprettet et websted til dig',
			button: 'Gå til mit nye websted :-)!'
		},function() {document.location=oo.community.Chrome.buildUserWebsiteURL(username)+'?edit=true&firstRun=true'});
	}
}

//////////////////////////////////// Community /////////////////////////////

oo.community.Gallery = function() {
	this.images = [];
}

oo.community.Gallery.prototype = {
	ignite : function() {
		$$('.image_gallery a').each(function(node,i) {
			node.observe('click',function(e) {this.onClickImage(i);e.stop();}.bind(this));
		}.bind(this))
	},
	registerImage : function(img,id) {
		this.images.push(img);
		$(id).observe('click',function(e) {this.onClickImage(img);e.stop();}.bind(this));
	},
	onClickImage : function(img) {
		this.getViewer().showById(img.id);
	},
	getViewer : function() {
		if (!this.imageViewer) {
			var v = this.imageViewer = In2iGui.ImageViewer.create();
			v.addDelegate(this);
			v.addImages(this.images);
		}
		return this.imageViewer;
	},
	resolveImageUrl : function(image,width,height) {
		return oo.baseContext+'/service/image/?id='+image.id+'&width='+width+'&height='+height;
	}
}

In2iGui.onDomReady(function() {oo.community.Chrome.get()});