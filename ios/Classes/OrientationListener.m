//
//  OrientationListener.m
//  native_device_orientation
//
//  Created by Sebastiaan de Smet on 21/01/2019.
//


#import "OrientationListener.h"

@implementation OrientationListener {
    UIDeviceOrientation lastDeviceOrientation;
    NSString* lastOrientation;
}

- (void)startOrientationListener:(void (^)(NSString* orientation)) orientationRetrieved {
    UIDevice *device = [UIDevice currentDevice];
    [device beginGeneratingDeviceOrientationNotifications];
    NSNotificationCenter *nc = [NSNotificationCenter defaultCenter];
    self.observer = [nc addObserverForName:UIDeviceOrientationDidChangeNotification object:nil queue:[NSOperationQueue mainQueue] usingBlock:^(NSNotification *notification) {

        NSString* orientation = [self getDeviceOrientation];

        if (![orientation isEqualToString:(self->lastOrientation)]) {
            self->lastOrientation = orientation;
            orientationRetrieved(orientation);
        }
    }];

    self-> lastOrientation = [self getDeviceOrientation];
    orientationRetrieved(lastOrientation);
}

- (void) getOrientation:(void (^)(NSString* orientation)) orientationRetrieved{
    NSString* orientation = [self getDeviceOrientation];
    orientationRetrieved(orientation);
}

- (NSString*)getDeviceOrientation {
    switch ([UIDevice currentDevice].orientation) {
        case UIDeviceOrientationUnknown:
            return UNKNOWN;
         case UIDeviceOrientationPortrait:
            return PORTRAIT_UP;
         case UIDeviceOrientationPortraitUpsideDown:
            return PORTRAIT_DOWN;
         case UIDeviceOrientationLandscapeLeft:
            return LANDSCAPE_LEFT;
        case UIDeviceOrientationLandscapeRight:
            return LANDSCAPE_RIGHT;
        case UIDeviceOrientationFaceUp:
            return FACE_UP;
        case UIDeviceOrientationFaceDown:
            return FACE_DOWN;

        // case UIInterfaceOrientationPortrait:
        //     return PORTRAIT_UP;
        // case UIInterfaceOrientationPortraitUpsideDown:
        //     return PORTRAIT_DOWN;
        // case UIInterfaceOrientationLandscapeRight:
        //     // return left for right as UIInterfaceOrientation is 'the amount needed
        //     // to rotate to get back to normal', not the actual rotation
        //     return LANDSCAPE_LEFT;
        // case UIInterfaceOrientationLandscapeLeft:
        //     // return right for left, see above
        //     return LANDSCAPE_RIGHT;
        default:
            return UNKNOWN;
            break;
    }
}

- (void)stopOrientationListener {
    NSNotificationCenter *nc = [NSNotificationCenter defaultCenter];
    UIDevice *device = [UIDevice currentDevice];
    [device endGeneratingDeviceOrientationNotifications];
    [nc removeObserver:self.observer];
    self.observer = NULL;
}

@end
