hui.on(() => {
  hui.control({
    id: document.querySelector("[data-id]").getAttribute('data-id'),
    components: {
      diagram: 'diagram'
    },
    'ready!'() {
      hui.ui.request({
        url : '/service/model/diagram',
        parameters : { id: this.id },
        $object: data => this.components.diagram.setData(data)
      })
    },
    'diagram.open!'(e) {
      document.location = '/browse?id=' + e.value.id;
    }
  })
})