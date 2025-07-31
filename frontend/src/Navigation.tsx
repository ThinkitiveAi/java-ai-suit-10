import React, { useState, useMemo } from 'react';
import {
  AppBar,
  Toolbar,
  Typography,
  IconButton,
  Box,
  Drawer,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Divider,
  Avatar,
  useMediaQuery,
  Stack,
  Button,
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import DashboardIcon from '@mui/icons-material/Dashboard';
import PeopleIcon from '@mui/icons-material/People';
import EventIcon from '@mui/icons-material/Event';
import AssessmentIcon from '@mui/icons-material/Assessment';
import SettingsIcon from '@mui/icons-material/Settings';
import { useTheme } from '@mui/material/styles';
import { Link, useLocation } from 'react-router-dom';
import SearchIcon from '@mui/icons-material/Search';
import NotificationsNoneIcon from '@mui/icons-material/NotificationsNone';
import ArrowDropDownIcon from '@mui/icons-material/ArrowDropDown';
import Menu from '@mui/material/Menu';
import MenuItem from '@mui/material/MenuItem';

const COLORS = {
  primary: '#0052CC',
  background: '#F4F5F7',
  textPrimary: '#172B4D',
  textSecondary: '#6B778C',
  border: '#DFE1E6',
  error: '#FF5630',
};

const NAV_ROUTES = [
  { label: 'Dashboard', path: '/dashboard', icon: <DashboardIcon /> },
  { label: 'Patients', path: '/patients', icon: <PeopleIcon /> },
  { label: 'Appointments', path: '/appointments', icon: <EventIcon /> },
  { label: 'Reports', path: '/reports', icon: <AssessmentIcon /> },
  { label: 'Settings', path: '/settings', icon: <SettingsIcon /> },
];

const NAV_TABS = [
  { label: 'Dashboard', path: '/dashboard' },
  { label: 'Patients', path: '/patients' },
  { label: 'Calendar', path: '/calendar', dropdown: true },
  { label: 'Billing', path: '/billing' },
  { label: 'Communication', path: '/communication' },
  { label: 'Reports', path: '/reports' },
  { label: 'Settings', path: '/settings' },
  { label: 'Review Notes', path: '/review-notes' },
];

const Navigation: React.FC = () => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [profileMenuAnchor, setProfileMenuAnchor] = useState<null | HTMLElement>(null);
  const location = useLocation();

  const handleCalendarClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };
  const handleCalendarClose = () => {
    setAnchorEl(null);
  };
  const handleProfileMenuClick = (event: React.MouseEvent<HTMLElement>) => {
    setProfileMenuAnchor(event.currentTarget);
  };
  const handleProfileMenuClose = () => {
    setProfileMenuAnchor(null);
  };

  const drawer = (
    <Box sx={{ width: 250, background: COLORS.background, height: '100%' }}>
      <Toolbar />
      <Divider />
      <List>
        {NAV_ROUTES.map((route) => (
          <ListItem
            
            key={route.path}
            component={Link}
            to={route.path}
            selected={location.pathname === route.path}
            onClick={() => setDrawerOpen(false)}
            sx={{
              '&.Mui-selected': {
                background: COLORS.primary,
                color: '#fff',
                '& .MuiListItemIcon-root': { color: '#fff' },
              },
            }}
          >
            <ListItemIcon>{route.icon}</ListItemIcon>
            <ListItemText primary={route.label} />
          </ListItem>
        ))}
      </List>
    </Box>
  );

  const navLinks = useMemo(() => (
    <Stack direction="row" spacing={2} alignItems="center">
      {NAV_ROUTES.map((route) => (
        <Button
          key={route.path}
          component={Link}
          to={route.path}
          sx={{
            color: location.pathname === route.path ? COLORS.primary : COLORS.textPrimary,
            fontWeight: 600,
            textTransform: 'none',
            borderBottom: location.pathname === route.path ? `2px solid ${COLORS.primary}` : 'none',
            borderRadius: 0,
            px: 2,
            py: 1,
            '&:hover': {
              background: COLORS.background,
              color: COLORS.primary,
            },
          }}
          startIcon={route.icon}
        >
          {route.label}
        </Button>
      ))}
    </Stack>
  ), [location.pathname]);

  const tabList = (
    <Stack direction="row" spacing={0} alignItems="flex-end" sx={{ height: 43 }}>
      {NAV_TABS.map((tab) => {
        const selected = location.pathname === tab.path;
        if (tab.dropdown) {
          return (
            <Box key={tab.label} sx={{ display: 'flex', alignItems: 'center', height: '100%' }}>
              <Button
                onClick={handleCalendarClick}
                endIcon={<ArrowDropDownIcon />}
                sx={{
                  borderRadius: '8px 8px 0 0',
                  background: selected ? '#EEF7FE' : 'transparent',
                  color: selected ? '#004FD9' : '#21262B',
                  fontFamily: 'Figtree',
                  fontWeight: 400,
                  fontSize: 14,
                  lineHeight: '1.2em',
                  px: 2,
                  py: 1.5,
                  minWidth: 120,
                  boxShadow: 'none',
                  borderBottom: selected ? '2px solid #004FD9' : '2px solid transparent',
                  textTransform: 'none',
                  height: 43,
                  '&:hover': {
                    background: '#EEF7FE',
                  },
                }}
              >
                {tab.label}
              </Button>
              <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={handleCalendarClose}>
                <MenuItem onClick={handleCalendarClose}>Calendar Option 1</MenuItem>
                <MenuItem onClick={handleCalendarClose}>Calendar Option 2</MenuItem>
              </Menu>
            </Box>
          );
        }
  return (
          <Button
            key={tab.label}
            component={Link}
            to={tab.path}
            sx={{
              borderRadius: '8px 8px 0 0',
              background: selected ? '#EEF7FE' : 'transparent',
              color: selected ? '#004FD9' : '#21262B',
              fontFamily: 'Figtree',
              fontWeight: selected ? 500 : 400,
              fontSize: 14,
              lineHeight: '1.2em',
              px: 2,
              py: 1.5,
              minWidth: 120,
              boxShadow: 'none',
              borderBottom: selected ? '2px solid #004FD9' : '2px solid transparent',
              textTransform: 'none',
              height: 43,
              '&:hover': {
                background: '#EEF7FE',
              },
            }}
          >
            {tab.label}
          </Button>
        );
      })}
          </Stack>
  );

  return (
    <Box sx={{ width: '100%', background: '#fff', borderBottom: '1px solid #E7E7E7', fontFamily: 'Figtree' }}>
      <Box sx={{ maxWidth: 1440, mx: 'auto', px: 2, py: 1, display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 2 }}>
                              <Stack direction="row" alignItems="center" spacing={2}>
          <Typography variant="h6" sx={{ color: '#0067FD', fontWeight: 700, fontFamily: 'Figtree', fontSize: 21.6, letterSpacing: 0 }}>
            Custom EHR
          </Typography>
          {!isMobile && tabList}
                              </Stack>
        <Stack direction="row" alignItems="center" spacing={1.5}>
          <IconButton size="large" sx={{ bgcolor: '#fff', borderRadius: 2 }}>
            <SearchIcon sx={{ color: '#373E41' }} />
                              </IconButton>
          <IconButton size="large" sx={{ bgcolor: '#fff', borderRadius: 2 }}>
            <NotificationsNoneIcon sx={{ color: '#373E41' }} />
                              </IconButton>
          <Stack direction="row" alignItems="center" spacing={1} sx={{ ml: 2 }}>
            <Avatar sx={{ bgcolor: '#750D8C', width: 38, height: 38, fontFamily: 'Figtree', fontWeight: 500, fontSize: 12 }}>JD</Avatar>
            <Typography sx={{ color: '#21262B', fontFamily: 'Figtree', fontWeight: 400, fontSize: 14 }}>John Doe</Typography>
            <IconButton size="small" onClick={handleProfileMenuClick} sx={{ p: 0.5 }}>
              <ArrowDropDownIcon sx={{ color: '#373E41' }} />
                              </IconButton>
            <Menu anchorEl={profileMenuAnchor} open={Boolean(profileMenuAnchor)} onClose={handleProfileMenuClose}>
              <MenuItem onClick={handleProfileMenuClose}>Profile</MenuItem>
              <MenuItem onClick={handleProfileMenuClose}>Logout</MenuItem>
            </Menu>
          </Stack>
        </Stack>
      </Box>
      {isMobile && (
        <Drawer anchor="left" open={drawerOpen} onClose={() => setDrawerOpen(false)}>
          <Box sx={{ width: 250, p: 2 }}>
            {tabList}
          </Box>
      </Drawer>
      )}
    </Box>
  );
};

export default Navigation; 