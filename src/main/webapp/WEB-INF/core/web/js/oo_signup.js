(function() {
  oo.SignUp = function() {
    this._show();
    oo.signUp = this._show.bind(this);
  }

  oo.SignUp.prototype = {
    _show : function() {
      if (!this._box) {
        var box = this._box = hui.ui.Panel.create({
          title: {en: 'Sign up', da: 'Bliv medlem'},
          closable: true,
          width: 400,
          padding: 15
        });
        var form = this._form = hui.ui.Formula.create();
        var group = form.buildGroup(null, [
          {
            type: 'TextInput',
            label: 'E-mail',
            options: {key: 'email', testName: 'signupEmail'}
          },
          {
            type: 'TextInput',
            label: {en: 'Username', da: 'Brugernavn'},
            options: {key:'username', testName: 'signupUsername'}
          },
          {
            type: 'TextInput',
            label: {en: 'Password', da: 'Kodeord'},
            options: {key:'password', secret: true, testName: 'signupPassword'}
          },
          {
            type: 'Checkbox',
            label: {en: 'Accept terms', da:'Acceptér vilkårene'},
            options: {key:'terms', testName: 'signupAccept', text:'dadad'}
          }
        ]);
        var termsLink = oo.Link.create({text: {en: 'Read the terms', da: 'Læs vilkårene'}});
        group.add(termsLink)
        var buttons = group.createButtons();
        var cancel = hui.ui.Button.create({text: {en: 'Cancel', da: 'Annuller'}, variant: 'light'});
        buttons.add(cancel);
        buttons.add(hui.ui.Button.create({
          text: {en: 'Sign up', da: 'Bliv medlem'},
          highlighted: true,
          variant: 'light',
          submit: true,
          testName: 'signupSubmit'
        }));
        box.add(form);
        form.listen({
          $submit : this._submit.bind(this)
        });
        cancel.listen({
          $click : function() {
            form.reset();
            box.hide();
          }
        })
        termsLink.listen({
          $click : function() {
            window.open(hui.find('.js-agreements').href);
          }
        })
      }
      this._box.show();
      this._form.focus();
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
          box.hide();
          setTimeout(function() {
            document.location.reload();
          },2000);
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
