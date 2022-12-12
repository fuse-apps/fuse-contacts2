import Contacts from "FuseJS/Contacts"

export default class ContactsExample {

    addContact() {
        Contacts.addContact({
            givenName: "Fuse X",
            phoneNumbers: [{
                label: "home",
                number: "+1234567890"
            }]
        })
    }
}
