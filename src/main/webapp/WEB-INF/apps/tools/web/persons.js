var personsController = {
  activePerson : 0,

  $ready : function() {
  },

  $select$selector : function(item) {
/*    if (item && item.value=='persons') {
      hui.ui.changeState('persons');
    } else if (item && item.value=='invitations') {
      hui.ui.changeState('invitations');
    }*/
  },
  $open$list : function(obj) {
    this.editPerson(obj.id);
  },

  //////////////////////////////// Persons /////////////////////////////

  $click$newPerson : function() {
    this.activePerson = 0;
    personFormula.reset();
    personWindow.show();
  },
  $click$cancelPerson : function() {
    this.activePerson = 0;
    personFormula.reset();
    personWindow.hide();
  },
  $click$deletePerson : function() {
    personWindow.setBusy(true);
    hui.ui.request({
      url : '/deletePerson',
      parameters : {id:this.activePerson},
      $success : function() {
        personFormula.reset();
        personWindow.hide();
        list.refresh();
      },
      $finally : function() {
        personWindow.setBusy(false);
      }
    });
  },
  $click$savePerson : function() {
    var person = personFormula.getValues();
    person.id=this.activePerson;

    var emails = personEmails.getValue();
    var phones = personPhones.getValue();
    var self = this;
    hui.ui.request({
      url : '/savePerson',
      json : { data : {
        person: person,
        emails: emails,
        phones: phones
      }},
      $success : function() {
        list.refresh();
        personFormula.reset();
        personWindow.hide();
      }
    });
  },
  editPerson : function(id) {
    this.activePerson = id;
    personFormula.reset();
    personWindow.show();
    hui.ui.request({
      url : '/loadPerson',
      parameters : {id:id},
      $object : function(person) {
        personFormula.setValues(person.person);
        personEmails.setValue(person.emails);
        personPhones.setValue(person.phones);
      }
    });
  },

  ///////////////////////////// Invitations /////////////////////////////

  _busySending : false,

  $click$newInvitation : function() {
    invitationFormula.reset();
    invitationWindow.show();
  },
  $click$sendInvitation : function() {
    var form = invitationFormula.getValues();
    invitationWindow.setBusy('Sending invitation');
    this._busySending = true;
    var self = this;
    hui.ui.request({
      url : "/createInvitation",
      parameters : {name: form.name, email: form.email, message: form.message},
      $object : function(invitation) {
        hui.ui.alert({
          emotion: 'smile',
          title: 'Invitationen er sendt!',
          text: 'Personen vil modtage en email med oplysninger om hvordan han/hun kan tilmelde sig!'
        });
        list.refresh();
        invitationWindow.hide();
      },
      $failure : function() {
        hui.ui.alert({text:'Unable to send the invitation'});
      },
      $finally : function() {
        invitationWindow.setBusy(false);
        self._busySending = false;
      }
    })
  }
}