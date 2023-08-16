# 🏗 🏗 🏗 THIS IS A DRAFT 🏗 🏗 🏗 


----


# Accessibility for FE app

🙏 we should aim to reach at least be AA on [WCAG 2.1 standards](https://www.w3.org/WAI/standards-guidelines/wcag/). If we reach AAA and/or are already looking into 2.2. - even better ❤️


## How do we make sure of that?
- we use the aiven component library components
- we use semantic html
- we use native elements where possible
- we do automated accessibility linting and testing
- we do manual testing for new features 
	- using tools to help us find issues
	- using assistive technology to see how it works for ourselves
- we do usability testing sessions with users that are used to assistive technology
- we collect resources in documentation for developers for best practices


### Automation tools
(tbc)

#### Linting
- eslint-plugin-jsx-a11y
- axe Accessibility Linter

#### Testing
- @axe-core/react

#### Manual testing
- Lighthouse (browser extension)
- axe DevTools - Web Accessibility Testing (browser extension)
- https://wave.webaim.org/extension/  (browser extension)
- Web Accessibility Checker (browser extension)
- https://validator.w3.org/
- https://raw.githubusercontent.com/Heydon/REVENGE.CSS/master/revenge.css

