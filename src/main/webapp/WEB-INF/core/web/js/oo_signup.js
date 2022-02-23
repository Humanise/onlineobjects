(function() {
  oo.SignUp = function() {
    this._show();
    oo.signUp = this._show.bind(this);
  }

  oo.SignUp.prototype = {
    _show : function() {
      if (this._box) {
        this._box.show();
        this._form.focus();
        return;
      }
      if (this._form) {
        this._form.focus();
        return;
      }
      var form = this._getForm();
      var buttons = hui.ui.Buttons.create();
      form.add(buttons);

      buttons.add(hui.ui.Button.create({
        text: {en: 'Sign up', da: 'Bliv medlem'},
        highlighted: true,
        submit: true,
        testName: 'signupSubmit'
      }));

      var container = hui.find('.js-signup-container');
      if (container) {
        container.appendChild(form.getElement());
      } else {
        var box = this._box = hui.ui.Panel.create({
          title: {en: 'Sign up', da: 'Bliv medlem'},
          closable: true,
          width: 400,
          padding: 15
        });
        box.add(form);
        this._box.show();
      }

      this._form.focus();
    },

    _getForm : function() {
      if (this._form) { return this._form; }
      var form = this._form = hui.ui.Form.create();
      var termsLink = oo.Link.create({text: {en: 'Read the terms', da: 'Læs vilkårene'}});
      var el = termsLink.getElement();
      el.style.marginLeft = '10px';
      el.style.verticalAlign = 'middle';
      termsLink.listen({
        $click : function() {
          window.open(hui.find('.js-agreements').href);
        }
      });
      var group = form.buildGroup({large:true}, [
        {
          type: 'TextInput',
          label: 'E-mail',
          options: {key: 'email', testName: 'signupEmail', large: true}
        },
        {
          type: 'TextInput',
          label: {en: 'Username', da: 'Brugernavn'},
          options: {key:'username', testName: 'signupUsername', large: true}
        },
        {
          type: 'TextInput',
          label: {en: 'Password', da: 'Kodeord'},
          options: {key:'password', secret: true, testName: 'signupPassword', large: true}
        },
        {
          type: 'Checkbox',
          options: {key:'terms', testName: 'signupAccept', text: {en: 'Accept the terms', da:'Acceptér vilkårene'}, size: 'large'},
          extra: [termsLink]
        }
      ]);
      form.listen({
        $submit : this._submit.bind(this)
      });
      return form;
    },

    _getWelcomeUrl : function() {
      var match = document.location.href.match(/^(https?:\/\/)[a-z]+(\.[a-z]+\.[a-z]+)/);
      return match[1] + "account" + match[2] + "/" + oo.language + "/welcome";
    },

    _submit : function(vars) {
      var form = this._form, box = this._box;
      var values = form.getValues();
      var failure = null;
      if (hui.isBlank(values.email)) {
        failure = {en: 'The e-mail is required', da: 'E-mail er krævet'}
      }
      else if (hui.isBlank(values.username)) {
        failure = {en: 'A username is required', da: 'Brugernavnet er krævet'}
      }
      else if (hui.isBlank(values.password)) {
        failure = {en: 'A password is reqired', da: 'Kodeordet er krævet'}
      }
      else if (!values.terms) {
        failure = {en: 'Please accept the terms', da: 'Acceptér veligst vilkårene'}
      }
      if (failure) {
        hui.ui.msg.fail({text: failure});
        form.focus();
        return
      }
      var welcomeUrl = this._getWelcomeUrl();
      hui.ui.msg({text:'Signing up...',busy:true});
      hui.ui.request({
        url : '/service/authentication/signup',
        parameters : {
          email: values.email,
          username: values.username,
          password: values.password
        },
        $success : function() {
          hui.ui.msg.success({text: {
            en: 'Welcome, look in your inbox :-)',
            da: 'Velkommen, se i din indbakke :-)'
          }});
          form.reset();
          if (box) {
            box.hide();
          }
          setTimeout(function() {
            document.location = welcomeUrl;
          }, 2000);
        },
        $failure : function(t, e) {
          var msg = (e ? e.message : null) || 'An unexpected error occurred'
          hui.ui.msg.fail({text: msg});
          form.focus();
        }
      })
    }
  };

  new oo.SignUp();
})();
