oo.TopBar = function(options) {
  this.options = options;
  this.element = hui.get(options.element);
  hui.ui.extend(this);
  this._attach();
  hui.ui.listen(this);
};

oo.TopBar.prototype = {
  _attach : function() {
    hui.on(this.element,'tap',this._onClick.bind(this));
  },
  _text : function(key) {
    if (!this._texts) {
      this._texts = hui.string.fromJSON(this.element.getAttribute('data-texts'));
    }
    return this._texts[key] || key;
  },
  _onClick : function(e) {
    e = hui.event(e);
    var a = e.findByTag('a');
    if (a) {
      var data = a.getAttribute('data');
      if (hui.cls.has(a,'is-selected')) {
        if (hui.window.getViewWidth() < 700) {
          e.prevent();
          this._showMenu();
        }
      }
      else if (data=='user') {
        e.prevent();
        if (this._userPanel && this._userPanel.isVisible()) {
          this._userPanel.hide();
        } else {
          this._showUserPanel(a)
        }
      }
      else if (data=='login') {
        e.prevent();
        this._showLoginPanel(a)
      }
      else if (data=='inbox') {
        e.prevent();
        this._showInbox(a)
      }
    }
  },
  _showMenu : function() {
    var self = this;
    if (!this._menu) {
      var menu = this._menu = hui.build('div.oo_topbar_drop',{
        parent: document.body,
        html: '<span class="oo_topbar_drop_title">OnlineObjects</span><span class="oo_topbar_drop_close oo_icon oo_icon_close_line"></span>'
      });
      hui.on(menu,'tap',function(e) {
        e = hui.event(e);
        if (!e.findByTag('a')) {
          hui.cls.remove(menu, 'is-visible');
        }
      })
      var links = hui.findAll('.oo_topbar_menu_link', this.element);
      for (var i = 0; i < links.length; i++) {
        var link = hui.build('a.oo_topbar_drop_link',{
          parent : menu,
          href : links[i].getAttribute('href')
        });
        hui.build('span',{'class':'oo_topbar_drop_icon oo_icon oo_icon_' + links[i].getAttribute('data-icon'), parent:link});
        hui.build('span',{'class':'oo_topbar_drop_label', text : hui.dom.getText(links[i]), parent:link});
      }
    }
    setTimeout(function() {
      hui.cls.add(self._menu, 'is-visible');
    },10)
  },

  _showUserPanel : function(a) {
    var panel = this._buildUserPanel();
    panel.show({target: a});
    this._updatePanel();
  },
  _buildUserPanel : function() {
    if (!this._userPanel) {
      var p = this._userPanel = hui.ui.Panel.create({
        width: 250,
        variant: 'light',
        padding: 10,
        autoHide: true
      });
      this._userInfoBlock = hui.build('div',{'class':'oo_topbar_info oo_topbar_info_busy'});
      p.add(this._userInfoBlock);
      var buttons = hui.build('div.oo_topbar_info_buttons');
      p.add(buttons);
      var logout = hui.ui.Button.create({
        text: this._text('log_out'),
        variant: 'light',
        testName: 'topbarLogOut',
        listener: {
          $click : this._doLogout.bind(this)
        }
      });
      buttons.appendChild(logout.element);
      var changeUser = hui.ui.Button.create({
        text: this._text('change_user'),
        variant: 'light',
        listener: {
          $click : function() {
            this._showLoginPanel(changeUser.getElement());
          }.bind(this)
        }
      });
      buttons.appendChild(changeUser.element);
    }
    return this._userPanel;
  },
  _updatePanel : function() {
    var node = this._userInfoBlock;
    hui.ui.request({
      url : '/service/authentication/getUserInfo',
      parameters : {language : oo.language},
      $object : function(info) {
        hui.cls.remove(node,'oo_topbar_info_busy')
        var html = '<div class="oo_topbar_info_photo">';
        html+='<div class="oo_topbar_info_photo_img"'
        if (info.photoId) {
          var ratio = window.devicePixelRatio > 1 ? 2 : 1;
          var size = 60 * ratio;
          var url = '/service/image/id'+info.photoId+'width' + size + 'height' + size + 'sharpen0.7cropped.jpg'
          html += ' style="background-image: url(' + url + ')"';
        }
        html+='></div>';
        html+='</div><div class="oo_topbar_info_content">'+
          '<p class="oo_topbar_info_name">'+hui.string.escape(info.fullName)+'</p>'+
        '<p class="oo_topbar_info_username">'+hui.string.escape(info.username)+'</p>';
        for (var i = 0; i < info.links.length; i++) {
          var link = info.links[i];
          html += '<p class="oo_topbar_info_account"><a class="oo_link" href="' + link.value + '"><span class="oo_link_text">' + hui.string.escape(this._text(link.label)) + '</span></a></p>';
        }
        html += '</div>';
        node.innerHTML = html;

      }.bind(this),
      $failure : function() {
        hui.cls.remove(node,'oo_topbar_info_busy');
        node.innerHTML = '<p>Error</p>';
      }
    });

    window.setTimeout(function() {

    }.bind(this),2000)
  },
  $showLogin : function() {
    var user = hui.find('.oo_topbar_user, .oo_topbar_login');
    if (user) {
      this._showLoginPanel(user);
    }
  },

  _showLoginPanel : function(a) {
    var panel = this._buildLoginPanel();
    if (panel.isVisible()) {
      panel.hide();
      return;
    }
    panel.show({target: a});
    this._loginForm.focus();
  },
  _buildLoginPanel : function() {
    if (!this._loginPanel) {
      var p = this._loginPanel = hui.ui.Panel.create({
        width: 200,
        variant: 'light',
        autoHide: true,
        padding: 10
      });

      var form = this._loginForm = hui.ui.Form.create({name:'topBarLoginForm'});
      form.buildGroup(null,[
        {type: 'TextInput', label: this._text('username'), options: {key: 'username',testName: 'topUsername'}},
        {type: 'TextInput', label: this._text('password'), options: {secret: true, key: 'password',testName: 'topPassword'}}
      ]);
      p.add(form);
      var login = hui.ui.Button.create({
        text: this._text('log_in'),
        variant: 'light',
        highlighted: true,
        name: 'topBarLoginButton'
      });
      hui.cls.add(login.getElement(),'oo_topbar_loginbutton');
      p.add(login);
      var forgot = hui.build('div.oo_topbar_forgot',{
        html:'<a class="oo_link" href="javascript://"><span>' + this._text('forgot_password') + '</span></a>'
      });
      hui.listen(forgot, 'click', function() {oo.recover()});
      p.add(forgot);
      var signup = hui.build('div.oo_topbar_signup',{
        html:'<a class="oo_link" href="javascript://"><span>' + this._text('create_account') + '</span></a>'
      });
      p.add(signup);
      hui.listen(signup, 'click', function() {oo.signUp()});
    }
    return this._loginPanel;
  },
  $submit$topBarLoginForm : function() {
    this._doLogin();
  },
  $click$topBarLoginButton : function() {
    this._doLogin();
  },
  _doLogin : function() {
    var values = this._loginForm.getValues();
    var missing = (hui.isBlank(values.username) ? 'username' : undefined) || (hui.isBlank(values.password) ? 'password' : undefined)
    if (missing) {
      this._loginForm.focus(missing);
      return;
    }
    this._loginPanel.setBusy();
    hui.ui.request({
      url : '/service/authentication/changeUser',
      parameters : {username:values.username,password:values.password},
      $success : function(response) {
        hui.ui.msg.success({text:this._text('you_are_logged_in')});
        this._afterLogin();
      }.bind(this),
      $failure : function(t) {
        var obj = hui.string.fromJSON(t.responseText);
        var msg = obj ? obj.message : 'An unexpected error occured'

        hui.ui.msg.fail({text: msg});
        this._loginPanel.setBusy(false);
      }.bind(this),
      $exception : function(e) {
        throw e;
      }
    })
  },
  _afterLogin : function() {
    var path = this.element.getAttribute('data-login-url');
    if (path) {
      document.location = path;
      return;
    }
    setTimeout(function() {
      document.location.reload();
    },500);
  },
  _doLogout : function() {
    this._userPanel.setBusy(true);
    hui.ui.request({
      credentials: true,
      url : '/service/authentication/logout',
      $success : function() {
        this._afterLogout();
      }.bind(this)
    })
  },
  _afterLogout : function() {
    var path = this.element.getAttribute('data-logout-url');
    if (path) {
      document.location = path;
      return;
    }
    document.location.reload();    
  },

  _showInbox : function(a) {
    if (!this._inboxPanel) {
      var p = this._inboxPanel = hui.ui.Panel.create({
        width: 200,
        variant: 'light',
        autoHide: true,
        padding: 5
      });
      //p.add(hui.build('div',{style:'height: 300px'}));
      var list = hui.ui.List.create({
        variant : 'light',
        source : new hui.ui.Source({url:'/service/model/listInbox'})
      });
      list.listen({
        $select : function(info) {
          document.location = info.data.url;
        },
        $clickIcon : function(info) {
          hui.ui.request({
            url : '/service/model/removeFromInbox',
            parameters : {id:info.row.id},
            $success : function() {
              list.refresh();
            }
          })
        }
      })
      p.add(list);

    }
    this._inboxPanel.show({target:a});
  }
};