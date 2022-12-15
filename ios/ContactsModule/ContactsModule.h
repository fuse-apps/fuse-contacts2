#import <Contacts/Contacts.h>
#import <ContactsUI/ContactsUI.h>

@interface ContactsModule : NSObject <CNContactViewControllerDelegate>

- (BOOL) addContact:(NSString *)json;

@end
