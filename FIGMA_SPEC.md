# UniSync Figma Build Spec

## Frame Setup

- Device: Android compact, `360 x 800`
- Grid: 4 columns, 16 px margin, 12 px gutter
- Outer screen padding: 18 px horizontal
- Screen Auto Layout: vertical, 16 px gap, fill container
- Bottom navigation: fixed to bottom, 18 px side margins, 64 px height
- Typography: Inter or Roboto

## Design Tokens

- Primary: `#006B3C`
- Secondary: `#6FCF97`
- Background: `#F7F9F8`
- Surface: `#FFFFFF`
- Text: `#222222`
- Muted text: `#66736D`
- Divider: `#E4EAE7`
- Warning: `#F2C94C`
- Danger: `#EB5757`
- Radius small: 12 px
- Radius medium: 18 px
- Radius large: 24 px
- Card shadow: `0 6 16 rgba(15,38,27,.08)`
- Elevated shadow: `0 12 28 rgba(15,38,27,.12)`

## Reusable Components

- Android status bar: time, signal, Wi-Fi, battery
- App bar: title stack + icon button
- Icon button: 42 x 42, 14 px radius
- Primary button: 52 px height, 16 px radius, primary fill
- Secondary button: 52 px height, 16 px radius, white fill, divider stroke
- Text field: 54 px height, 16 px radius, icon + label
- Card: vertical Auto Layout, 14 px padding, 10 px gap
- Chip: horizontal Auto Layout, 26 px min height, pill radius
- Assignment row: 3-column Auto Layout, priority color strip
- Bottom nav item variants: default, active
- Crowd status variants: low, medium, high
- Day tab variants: default, selected
- Toggle variants: on, off

## Screens

1. Splash Screen: centered logo, app name, short tagline, loading progress.
2. Login/Register: DLSU email field, password field, login CTA, register secondary CTA.
3. Dashboard/Home: upcoming class hero, quick actions, assignment cards, crowd summaries.
4. Schedule Manager: weekday selector and class timeline cards.
5. Task & Assignment Tracker: segmented filter, featured due task, task list, FAB.
6. Campus Map Navigation: search field, simplified map, clickable markers, building info, walking time.
7. Crowd Monitoring: low/yellow/red status cards and report crowd level control.
8. QR Classroom Check-In: large scanner surface, scan frame, confirmation modal.
9. Notifications: filtered notification feed with type icons.
10. Profile & Settings: student identity card, toggles, settings rows.

## Figma Auto Layout Notes

- Apply Auto Layout to every screen root, card, button, list row, chip, tab, and nav item.
- Use vertical stacks for page content and horizontal stacks for app bars, rows, chips, and nav.
- Cards should use `Fill container` width and `Hug contents` height.
- Lists should use consistent 9-12 px vertical gaps.
- Use component variants for `Button/Primary`, `Button/Secondary`, `Crowd/Low`, `Crowd/Medium`, `Crowd/High`, `Nav/Default`, and `Nav/Active`.
- Keep all text within fixed Android width by using max two-line labels and smaller 12-16 px body text inside compact components.
