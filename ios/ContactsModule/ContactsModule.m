#import <AddressBook/AddressBook.h>
#import <UIKit/UIKit.h>
#import <ContactsModule/ContactsModule.h>
#import <AssetsLibrary/AssetsLibrary.h>

@implementation ContactsModule {
    CNContactStore * contactStore;

    BOOL notesUsageEnabled;
}

- (BOOL) addContact:(NSString *)json
{
    NSError *error;
    NSDictionary *contactData = [NSJSONSerialization
                                 JSONObjectWithData: [json dataUsingEncoding:NSUTF8StringEncoding]
                                 options: kNilOptions
                                 error: &error];

    if (error) {
        NSLog(@"Error: %@", [error localizedDescription]);
        return FALSE;
    }

    CNContactStore* contactStore = [self contactsStore];
    if (!contactStore)
        return FALSE;

    CNMutableContact * contact = [[CNMutableContact alloc] init];

    [self updateRecord:contact withData:contactData];

    @try {
        CNSaveRequest *request = [[CNSaveRequest alloc] init];
        [request addContact:contact toContainerWithIdentifier:nil];

        [contactStore executeSaveRequest:request error:nil];

        return TRUE;
    }
    @catch (NSException *exception) {
        NSLog(@"Error: %@", [exception reason]);
        return FALSE;
    }
}

-(void) updateRecord:(CNMutableContact *)contact withData:(NSDictionary *)contactData
{
    NSString *givenName = [contactData valueForKey:@"givenName"];
    NSString *familyName = [contactData valueForKey:@"familyName"];
    NSString *middleName = [contactData valueForKey:@"middleName"];
    NSString *company = [contactData valueForKey:@"company"];
    NSString *jobTitle = [contactData valueForKey:@"jobTitle"];

    NSDictionary *birthday = [contactData valueForKey:@"birthday"];

    contact.givenName = givenName;
    contact.familyName = familyName;
    contact.middleName = middleName;
    contact.organizationName = company;
    contact.jobTitle = jobTitle;

    if (notesUsageEnabled){
        NSString *note = [contactData valueForKey:@"note"];
        contact.note = note;
    }

    if (birthday) {
        NSDateComponents *components;
        if (contact.birthday != nil) {
            components = contact.birthday;
        } else {
            components = [[NSDateComponents alloc] init];
        }
        if (birthday[@"month"] && birthday[@"day"]) {
            if (birthday[@"year"]) {
                components.year = [birthday[@"year"] intValue];
            }
            components.month = [birthday[@"month"] intValue];
            components.day = [birthday[@"day"] intValue];
        }

        contact.birthday = components;
    }

    NSMutableArray *phoneNumbers = [[NSMutableArray alloc]init];

    for (id phoneData in [contactData valueForKey:@"phoneNumbers"]) {
        NSString *label = [phoneData valueForKey:@"label"];
        NSString *number = [phoneData valueForKey:@"number"];

        CNLabeledValue *phone;
        if ([label isEqual: @"main"]){
            phone = [[CNLabeledValue alloc] initWithLabel:CNLabelPhoneNumberMain value:[[CNPhoneNumber alloc] initWithStringValue:number]];
        }
        else if ([label isEqual: @"mobile"]){
            phone = [[CNLabeledValue alloc] initWithLabel:CNLabelPhoneNumberMobile value:[[CNPhoneNumber alloc] initWithStringValue:number]];
        }
        else if ([label isEqual: @"iPhone"]){
            phone = [[CNLabeledValue alloc] initWithLabel:CNLabelPhoneNumberiPhone value:[[CNPhoneNumber alloc] initWithStringValue:number]];
        }
        else{
            phone = [[CNLabeledValue alloc] initWithLabel:label value:[[CNPhoneNumber alloc] initWithStringValue:number]];
        }

        [phoneNumbers addObject:phone];
    }
    contact.phoneNumbers = phoneNumbers;


    NSMutableArray *urls = [[NSMutableArray alloc]init];

    for (id urlData in [contactData valueForKey:@"urlAddresses"]) {
        NSString *label = [urlData valueForKey:@"label"];
        NSString *url = [urlData valueForKey:@"url"];

        if (label && url) {
            [urls addObject:[[CNLabeledValue alloc] initWithLabel:label value:url]];
        }
    }

    contact.urlAddresses = urls;


    NSMutableArray *emails = [[NSMutableArray alloc]init];

    for (id emailData in [contactData valueForKey:@"emailAddresses"]) {
        NSString *label = [emailData valueForKey:@"label"];
        NSString *email = [emailData valueForKey:@"email"];

        if (label && email) {
            [emails addObject:[[CNLabeledValue alloc] initWithLabel:label value:email]];
        }
    }

    contact.emailAddresses = emails;

    NSMutableArray *postalAddresses = [[NSMutableArray alloc]init];

    for (id addressData in [contactData valueForKey:@"postalAddresses"]) {
        NSString *label = [addressData valueForKey:@"label"];
        NSString *street = [addressData valueForKey:@"street"];
        NSString *postalCode = [addressData valueForKey:@"postCode"];
        NSString *city = [addressData valueForKey:@"city"];
        NSString *country = [addressData valueForKey:@"country"];
        NSString *state = [addressData valueForKey:@"state"];

        if (label && street) {
            CNMutablePostalAddress *postalAddr = [[CNMutablePostalAddress alloc] init];
            postalAddr.street = street;
            postalAddr.postalCode = postalCode;
            postalAddr.city = city;
            postalAddr.country = country;
            postalAddr.state = state;
            [postalAddresses addObject:[[CNLabeledValue alloc] initWithLabel:label value: postalAddr]];
        }
    }

    contact.postalAddresses = postalAddresses;

    NSMutableArray<CNLabeledValue<CNInstantMessageAddress*>*> *imAddresses = [[NSMutableArray alloc] init];

    for (id imData in [contactData valueForKey:@"imAddresses"]) {
        NSString *service = [imData valueForKey:@"service"];
        NSString *username = [imData valueForKey:@"username"];

        if (service && username) {
            CNLabeledValue *imAddress = [[CNLabeledValue alloc] initWithLabel: @"instantMessageAddress" value: [[CNInstantMessageAddress alloc] initWithUsername: username service: service]];
            [imAddresses addObject: imAddress];
        }
    }

    contact.instantMessageAddresses = imAddresses;
}

-(CNContactStore*) contactsStore {
    if (!contactStore) {
        CNContactStore* store = [[CNContactStore alloc] init];

        contactStore = store;
    }
    if (!contactStore.defaultContainerIdentifier) {
        NSLog(@"warn - no contact store container id");

        CNAuthorizationStatus authStatus = [CNContactStore authorizationStatusForEntityType:CNEntityTypeContacts];
        if (authStatus == CNAuthorizationStatusDenied || authStatus == CNAuthorizationStatusRestricted){
            NSLog(@"Error: %@", @"denied");
        } else {
            NSLog(@"Error: %@", @"undefined");
        }

        return nil;
    }

    return contactStore;
}

@end
