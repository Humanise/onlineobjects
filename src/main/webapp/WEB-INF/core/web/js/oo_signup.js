(function() {
  oo.SignUp = function() {
    this._show();
    oo.signUp = this._show.bind(this);
  }

  oo.SignUp.prototype = {
    _show : function() {
      if (!this._box) {
        var box = this._box = hui.ui.Box.create({
          modal: true,
          title: 'Sign up',
          closable: true,
          absolute: true,
          width: 400,
          padding: 15
        });
        var form = this._form = hui.ui.Formula.create();
        var group = form.buildGroup(null,[
          {
            type: 'TextInput',
            label: 'E-mail',
            options: {key: 'email', testName: 'signupEmail'}
          },
          {
            type: 'TextInput',
            label: 'Username',
            options: {key:'username', testName: 'signupUsername'}
          },
          {
            type: 'TextInput',
            label: 'Password',
            options: {key:'password', secret: true, testName: 'signupPassword'}
          },
          {
            type: 'Checkbox',
            label: 'Accept terms',
            options: {key:'terms', testName: 'signupAccept'}
          }
        ]);
        var termsLink = oo.Link.create({text: 'Read the terms'});
        group.add(termsLink)
        var buttons = group.createButtons();
        var cancel = hui.ui.Button.create({text:'Cancel'});
        buttons.add(cancel);
        buttons.add(hui.ui.Button.create({text:'Signup', highlighted:true, submit:true, testName: 'signupSubmit'}));
        box.add(form);
        box.addToDocument();
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
        failure = "The e-mail is required"
      }
      else if (hui.isBlank(values.username)) {
        failure = "A username is required"
      }
      else if (hui.isBlank(values.password)) {
        failure = "A password is reqired"
      }
      else if (!values.terms) {
        failure = "Please accept the terms"
      }
      if (failure) {
        hui.ui.msg.fail({text: failure});
        form.focus();
        return
      }

      hui.ui.msg({text:'Signing up...',busy:true});
      hui.ui.request({
        url : oo.baseContext+'/service/authentication/signup',
        parameters : {
          email: values.email,
          username: values.username,
          password: values.password
        },
        $success : function() {
          hui.ui.msg.success({text:'Welcome, look in your inbox :-)'});
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
