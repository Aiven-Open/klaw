const NO_RESTRICTED_IMPORTS_RULES = ["error", {
    patterns: [{
        id: "DENY_PAGES_IMPORT",
        group: ["src/pages*"],
        message: "Pages should be only imported from '/src/router.tsx'"
    }]
}]

function isObject(value) {
    return typeof value === 'object' &&
    !Array.isArray(value) &&
    value !== null
}

const hasPatterns = (value) => isObject(value) && "patterns" in value;
const dropIdFromPatterns = (patterns) => patterns.map(({group, message}) => ({group, message}))

function filterPatternsByIds(patterns, ids) {
    return patterns.filter(pattern => {
        if ("id" in pattern) {
            return !ids.includes(pattern.id)
        }
        return true
    })
}

function strip_ids_from_no_restricted_imports(configuration) {
    return configuration.map((rule) => {
        if (hasPatterns(rule)) {
            return {...rule, patterns: dropIdFromPatterns(rule.patterns)}
        }
        return rule
    })
}

function filter_patterns_for_ids(configuration, ids) {
    return configuration.map(rule => {
        if (hasPatterns(rule)) {
            return {...rule, patterns: dropIdFromPatterns(filterPatternsByIds(rule.patterns, ids))}
        }
        return rule
    })
}

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
        "plugin:import/recommended",
        "plugin:import/typescript",
        "prettier"
    ],
    "ignorePatterns": ["dist/**", "types/api.d.ts"],
    "overrides": [
        {
            "files": ["src/router.tsx", "src/pages/**/*.test.tsx"],
            "rules": {
                "no-restricted-imports": filter_patterns_for_ids(NO_RESTRICTED_IMPORTS_RULES, ["DENY_PAGES_IMPORT"])
            }
        }
    ],
    "parser": "@typescript-eslint/parser",
    "parserOptions": {
        "ecmaVersion": "latest",
        "sourceType": "module"
    },
    "plugins": [
        "react",
        "@typescript-eslint",
        "no-relative-import-paths",
        "lodash"
    ],
    "settings": {
        "react": {
            "version": "detect"
        },
        "import/resolver": {
           "typescript": true,
            "node": true
        }
    },
    "rules": {
        "no-relative-import-paths/no-relative-import-paths": [
            "error"
        ],
        "no-unused-vars": "off",
        "no-restricted-imports": strip_ids_from_no_restricted_imports(NO_RESTRICTED_IMPORTS_RULES),
        "@typescript-eslint/no-unused-vars": "error",
        "@typescript-eslint/no-explicit-any": "error",
        "import/no-mutable-exports": "error",
        "import/no-cycle": "error",
        "import/exports-last": "error",
        "import/no-anonymous-default-export": "error",
        "import/group-exports": "error",
        "lodash/import-scope": "error",
        "react/prop-types": 0,
        "import/no-named-as-default": "error"
}
}
