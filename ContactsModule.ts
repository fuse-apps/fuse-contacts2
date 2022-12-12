/** Native contacts module - implemented by ContactsModule.uno */
declare module "FuseJS/Contacts" {

    function addContact(contact: Contact)

    interface EmailAddress {
        label: EmailLabel
        email: string
    }

    interface PhoneNumber {
        label: PhoneLabel
        number: string
    }

    interface PostalAddress {
        label: PostalAddressLabel
        street?: string
        city?: string
        state?: string
        postCode?: string
        country?: string
    }

    interface InstantMessageAddress {
        username?: string
        service?: string
    }

    interface Contact {
        company?: string
        emailAddresses?: EmailAddress[]
        familyName?: string
        givenName?: string
        middleName?: string
        jobTitle?: string
        phoneNumbers?: PhoneNumber[]
        prefix?: string
        suffix?: string
        department?: string
        imAddresses?: InstantMessageAddress[]
        note?: string
        postalAddresses?: PostalAddress[]
    }

    type EmailLabel = "home" | "work" | "mobile" | "other" | "personal"
    type PhoneLabel = "home" | "work" | "mobile" | "main" | "work fax" | "home fax" | "pager" | "work_pager" | "work_mobile" | "other" | "cell"
    type PostalAddressLabel = "home" | "work"
}
