module.exports = {
    "env": {
        "browser": true,
        "es2021": true
    },
    "extends": [
        "eslint:recommended",
        "plugin:react/recommended",
        "plugin:@typescript-eslint/recommended",
        "plugin:react/jsx-runtime",
        "prettier"
    ],
    "overrides": [
    ],
    "parser": "@typescript-eslint/parser",
    "parserOptions": {
        "ecmaVersion": "latest",
        "sourceType": "module"
    },
    "plugins": [
        "react",
        "@typescript-eslint",
        "no-relative-import-paths"
    ],
    "settings": {
        "react": {
            "version": "detect"
        }
    },
    "rules": {
        "no-relative-import-paths/no-relative-import-paths": [
            "error"
        ],
        "no-restricted-imports": ["error", {
            "patterns": [{
              "group": ["src/pages*"],
              "message": "Pages should be only imported from '/src/router.tsx'"
            }]
        }]
    }
}
