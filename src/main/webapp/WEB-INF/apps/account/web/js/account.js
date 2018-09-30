hui.ui.listen({

  msg : {
    sending_confirm: {en:'Sending confirmation request...', da:'Sender godkendelses-anmodning...'},
    check_inbox: {en:'Please check your inbox', da:'Se venligst i din indbakke'},
    unexpected_error: {en:'An unexpected error occured', da:'Der skete en uventet fejl'},
    password_changed: {en:'Your password is changed', da:'Dit kodeord er nu ændret'}
  },
  _getError : function(t) {
    var obj = hui.string.fromJSON(t.responseText);
    return obj ? obj.message : this.msg.unexpected_error;
  },

  // E-mail...

  $submit$mailForm : function() {
    var values = hui.ui.get('mailForm').getValues();
    hui.ui.request({
      url : '/changePrimaryEmail',
      parameters : {email:values.mail},
      message : {
        start: this.msg.sending_confirm,
        success: this.msg.check_inbox
      },
      $success : function() {
        hui.ui.get('emailPages').next();
      },
      $failure : function(t) {
        hui.ui.msg.fail({text: this._getError(t)});
      }.bind(this)
    })
  },
  $click$cancelChangeEmail : function() {
    hui.ui.get('emailPages').next();
  },
  $click$changeEmail : function() {
    hui.ui.get('emailPages').next();
    setTimeout(function() {
      hui.ui.get('mailForm').focus();
    },200)
  },

  $click$confirmEmail : function() {
    hui.ui.request({
      url : '/confirmEmail',
      message : {
        start: this.msg.sending_confirm,
        success: this.msg.check_inbox
      },
      $failure : function(t) {
        hui.ui.msg.fail({text: this._getError(t)});
      }.bind(this)
    })
  },

  // Name...

  $click$changeName : function() {
    hui.ui.get('namePages').next();
    setTimeout(function() {
      hui.ui.get('nameForm').focus();
    },200)
  },
  $click$cancelChangeName : function() {
    hui.ui.get('namePages').next();
  },
  $submit$nameForm : function() {
    var values = hui.ui.get('nameForm').getValues();
    hui.ui.request({
      url : '/changeName',
      parameters : {
        first: values.first,
        middle: values.middle,
        last: values.last
      },
      $success : function() {
        document.location.reload();
      },
      $failure : function(t) {
        hui.ui.msg.fail({text: this._getError(t)});
      }.bind(this)
    })
  },

  // Password...

  $click$changePassword : function() {
    hui.ui.get('passwordPages').next();
    setTimeout(function() {
      hui.ui.get('passwordForm').focus();
    },200)
  },

  $click$cancelPassword : function() {
    hui.ui.get('passwordPages').next();
  },

  $submit$passwordForm : function(form) {
    var values = form.getValues();
    hui.ui.request({
      url : '/changePassword',
      parameters : {
        currentPassword : values.currentPassword,
        newPassword : values.newPassword
      },
      $success : function() {
        hui.ui.msg.success({text:this.msg.password_changed});
        form.reset();
      }.bind(this),
      $failure : function(t) {
        hui.ui.msg.fail({text: this._getError(t)});
        form.focus();
      }.bind(this)
    })
  },
});