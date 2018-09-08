var accountView = {

  // E-mail...

  $submit$mailForm : function() {
    var values = hui.ui.get('mailForm').getValues();
    hui.ui.request({
      url : oo.baseContext+'/changePrimaryEmail',
      parameters : {email:values.mail},
      message : {
        start: 'Sending confirmation...',
        success: 'Check your inbox'
      },
      $success : function() {
        hui.ui.get('emailPages').next();
      },
      $failure : function(t) {
        var obj = hui.string.fromJSON(t.responseText);
        hui.ui.msg({text:obj ? obj.message : 'Something bad happened',icon:'common/warning',duration:3000});
      }
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
      url : oo.baseContext+'/confirmEmail',
      message : {
        start: 'Sending confirmation...',
        success: 'Check your inbox'
      },
      $failure : function() {
        hui.ui.msg.fail({text:'Unable to send mail'})
      }
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
      url : oo.baseContext+'/changeName',
      parameters : {
        first: values.first,
        middle: values.middle,
        last: values.last
      },
      $success : function() {
        document.location.reload();
      },
      $failure : function() {
        hui.ui.msg.fail({text:'Unable to change name'})
      }
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
      url : oo.baseContext+'/changePassword',
      parameters : {
        currentPassword : values.currentPassword,
        newPassword : values.newPassword
      },
      $success : function() {
        hui.ui.msg({text:'Your password is changed',icon:'common/success',duration:3000});
        form.reset();
      },
      $failure : function(e) {
        hui.log(e);
        hui.ui.msg({text:'Unable to change password',icon:'common/warning',duration:3000});
        form.focus();
      }
    })
  },
};
hui.ui.listen(accountView);