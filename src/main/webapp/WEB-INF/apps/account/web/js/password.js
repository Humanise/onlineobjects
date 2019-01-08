var passwordView = {

  key : null,

  $ready : function() {
    this.key = hui.get('js-account-data').getAttribute('data-key');
    var form = hui.ui.get('resetPasswordForm');
    if (form) {
      form.focus();
    }
  },
  $submit$resetPasswordForm : function(form) {
    var values = form.getValues();
    if (hui.isBlank(values.password) || hui.isBlank(values.passwordAgain)) {
      form.focus();
      hui.ui.msg.fail({text:{da:'Udfyld venligst begge felter',en:'Please fill in both fields'}});
      return;
    }
    if (values.password!==values.passwordAgain) {
      form.focus();
      hui.ui.msg.fail({text:{da:'De to kodeord er ikke ens',en:'The passwords are not equal'}});
      return;
    }
    hui.ui.request({
      url : '/changePasswordUsingKey',
      parameters : {
        key : this.key,
        password : values.password
      },
      $success : function() {
        hui.ui.msg.success({text:{da:'Din kode er ændret',en:'Your password is changed'}});
        form.reset();
        setTimeout(function() {
          document.location = '/';
        },1000);
      },
      $failure : function(a,b) {
        var text = b ? b.message : null;
        if (!text) {
          text = {da:'Det lykkedes ikke, ',en:'It failed'};
        }
        hui.ui.msg.fail({text:text});
        form.focus();
      }
    })
  }
}
hui.ui.listen(passwordView);