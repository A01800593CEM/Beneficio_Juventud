export const getProfileBasedRedirect = (profile?: string): string => {
  switch (profile) {
    case "admin":
      return "/admin";
    case "colaborator":
    case "collaborator":
      return "/colaborator";
    default:
      return "/usuario";
  }
};

export const redirectBasedOnProfile = (profile?: string) => {
  const redirectUrl = getProfileBasedRedirect(profile);
  if (typeof window !== "undefined") {
    window.location.href = redirectUrl;
  }
  return redirectUrl;
};