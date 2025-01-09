(function() {
  var ctrl = {
    init: function() {
      this.check();
    },

    _rootUrl : 'https://' + document.location.host.replace(/^[a-z]+/, 'account'),

    check : function() {
      var self = this;
      hui.on(['hui.ui'],function() {
        hui.ui.request({
          url: self._rootUrl + '/status',
          credentials: true,
          $object : function(info) {
            self.draw(info);
          }.bind(this),
          $failure : function() {
            console.error(arguments);
          }
        })
      });
    },
    _itemMarkup : function(info) {
      if (info.username == 'public') {
        return '<a href="javascript://" class="oo_topbar_item oo_topbar_login" data="login">Log in</a>'
      } else {
        return '<a href="javascript://" class="oo_topbar_item oo_topbar_user" data="user">' +
          '<span class="oo_icon oo_icon-16 oo_icon-user oo_topbar_user_icon"></span>' +
          hui.string.escape(info.displayName) +
        '</a>'
      }
    },
    draw : function(info) {
      var right = hui.find('.oo_topbar_right');
      var item = hui.build('li', {
        parent: right,
        'class': 'oo_topbar_right_item',
        html: this._itemMarkup(info)
      })
      var a = hui.find('a', item);
      hui.listen(a, 'click', function(e) {
        e.preventDefault();
        this._showLoginPanel(a);
      }.bind(this))
    },

    _showLoginPanel : function(a) {
      document.location = this._rootUrl;
      return;
      hui.ui.require(['Panel','Form','Button','TextInput'], function() {
        var panel = this._buildLoginPanel();
        if (panel.isVisible()) {
          panel.hide();
          return;
        }
        panel.show({target: a});
        this._loginForm.focus();
      }.bind(this));
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
        login.getElement().style.marginTop = '5px';
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
    _text : function(key) {
      return key;
    }
  }
  ctrl.init();
})();

