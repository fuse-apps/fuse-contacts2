import Contacts from "FuseJS/Contacts"

export default class ContactsExample {
    message: string

    addContact() {
        Contacts.requestPermission()
            .then(hasPermission => {
                if (hasPermission) {
                    Contacts.addContact({
                        givenName: "Fuse X",
                        phoneNumbers: [{
                            label: "home",
                            number: "+1234567890"
                        }]
                    })
                    this.message = "Contact added!"
                } else {
                    this.message = "Permission denied"
                }
            })
            .catch(reason => {
                this.message = "Error occured"
                console.error(reason)
            })
    }
}
