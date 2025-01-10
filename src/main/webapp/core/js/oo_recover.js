(function() {
  oo.Recover = function() {
    this._show();
    oo.recover = this._show.bind(this);
  }

  oo.Recover.prototype = {
    _show : function() {
      if (!this._panel) {
        var box = this._panel = hui.ui.Panel.create({
          title: hui.ui.getTranslated({
            en: 'I forgot my password',
            da: 'Jeg har glemt min kode'
          }),
          closable: true,
          width: 400,
          padding: 15
        });
        box.add(hui.build(
          'div.oo_topbar_forgot_intro', {
            text: hui.ui.getTranslated({
              en: 'Please provide either your username or your e-mail. We will then mail you instructions on how to change your password.',
              da: 'Angiv venligst din e-mail eller brugernavn. Så sender vi instruktioner om hvordan du kan ændre din kode.'
            })
        }));
        var form = this._form = hui.ui.Form.create();
        form.buildGroup(null,[{
          type: 'TextInput',
          label: {en: 'Username or e-mail:', da: 'Brugernavn eller e-mail:'},
          options: {key: 'usernameOrMail', name: 'ooTopBarUsernameOrMail'}
        }]);
        var buttons = form.createButtons();
        var cancel = hui.ui.Button.create({
          text: {en: 'Cancel', da: 'Annuller'},
          variant: 'light'
        });
        buttons.add(cancel);
        buttons.add(hui.ui.Button.create({
          text: {en: 'Send', da: 'Afsend'},
          highlighted: true,
          submit: true,
          variant: 'light'
        }));
        box.add(form);
        form.listen({
          $submit : function(vars) {
            var values = form.getValues();
            if (hui.isBlank(values.usernameOrMail)) {
              form.focus();
              hui.ui.stress(hui.ui.get('ooTopBarUsernameOrMail'));
              return;
            }

            hui.ui.msg({busy: true, text: {
              en: 'Let\'s see if we can find you...',
              da: 'Lad os se om vi kan finde dig...'
            }});
            hui.ui.request({
              url : '/service/authentication/recoverPassword',
              parameters : {usernameOrMail: values.usernameOrMail},
              $success : function() {
                hui.ui.msg.success({text: {
                  en: 'Look in your inbox :-)',
                  da: 'Se i din indbakke :-)'
                }});
                form.reset();
                box.hide();
              },
              $failure : function() {
                hui.ui.msg.fail({text: {
                  en: 'We could not find you, please try something else',
                  da: 'Vi kunne ikke finde dig, prøv venligst noget andet'
                }});
                form.focus();
              }
            })
          }
        });
        cancel.listen({
          $click : function() {
            form.reset();
            box.hide();
          }
        })
      }
      this._panel.show();
      this._form.focus();
    }
  };

  new oo.Recover();
})();
